package com.knowave.cashboard.domains.assetgoal.service

import com.knowave.cashboard.common.entity.BaseEntity
import com.knowave.cashboard.common.exception.AssetGoalNotFoundException
import com.knowave.cashboard.common.exception.DuplicateSavingRecordException
import com.knowave.cashboard.common.exception.InvalidSavingPeriodException
import com.knowave.cashboard.common.exception.InvalidTargetMonthException
import com.knowave.cashboard.domains.account.entity.Account
import com.knowave.cashboard.domains.account.repository.AccountRepository
import com.knowave.cashboard.domains.assetgoal.calculator.AssetGoalCalculator
import com.knowave.cashboard.domains.assetgoal.entity.AssetGoal
import com.knowave.cashboard.domains.assetgoal.entity.SavingRecord
import com.knowave.cashboard.domains.assetgoal.repository.AssetGoalRepository
import com.knowave.cashboard.domains.assetgoal.repository.SavingRecordRepository
import com.knowave.cashboard.domains.assetgoal.service.dto.AssetGoalSimulationCommand
import com.knowave.cashboard.domains.assetgoal.service.dto.CreateAssetGoalCommand
import com.knowave.cashboard.domains.assetgoal.service.dto.CreateSavingRecordCommand
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.dao.DataIntegrityViolationException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.util.UUID

class AssetGoalServiceImplTest {
	private val assetGoalRepository = FakeAssetGoalRepository()
	private val savingRecordRepository = FakeAssetGoalSavingRecordRepository()
	private val accountRepository = FakeAccountRepository()
	private val assetGoalServiceImpl = AssetGoalServiceImpl(
		assetGoalRepository = assetGoalRepository,
		savingRecordRepository = savingRecordRepository,
		accountRepository = accountRepository,
		assetGoalCalculator = AssetGoalCalculator(),
	)

	@Test
	fun `자산 목표를 생성하고 현재 자산 기준 분석값을 반환한다`() {
		accountRepository.accounts = listOf(Account("현금", "LIQUID", 4_000_000L))

		val result = assetGoalServiceImpl.createAssetGoal(
			CreateAssetGoalCommand(
				name = "1천만원 만들기",
				targetAmount = 10_000_000L,
				targetDate = LocalDate.now().plusMonths(6),
			),
		)

		assertThat(result.name).isEqualTo("1천만원 만들기")
		assertThat(result.currentAssetAmount).isEqualTo(4_000_000L)
		assertThat(result.remainingAmount).isEqualTo(6_000_000L)
		assertThat(result.achievementRate).isEqualTo(40.0)
	}

	@Test
	fun `없는 자산 목표 조회는 CustomException으로 처리한다`() {
		assertThatThrownBy { assetGoalServiceImpl.getAssetGoalDetail(UUID.randomUUID(), 3) }
			.isInstanceOf(AssetGoalNotFoundException::class.java)
	}

	@Test
	fun `시뮬레이션은 결과만 계산하고 저장하지 않는다`() {
		val assetGoalId = UUID.randomUUID()
		val targetDate = LocalDate.now().plusMonths(6)
		assetGoalRepository.save(AssetGoal("1천만원 만들기", 10_000_000L, targetDate).apply {
			assignBaseFields(assetGoalId)
		})
		accountRepository.accounts = listOf(Account("현금", "LIQUID", 4_000_000L))
		val toTargetMonth = YearMonth.now().minusMonths(1)
		savingRecordRepository.save(SavingRecord(toTargetMonth.toString(), 700_000L, null))
		savingRecordRepository.save(SavingRecord(toTargetMonth.minusMonths(1).toString(), 500_000L, null))

		val result = assetGoalServiceImpl.simulateAssetGoal(
			assetGoalId = assetGoalId,
			command = AssetGoalSimulationCommand(
				monthlySavingAmount = 2_000_000L,
			),
		)

		assertThat(result.monthlySavingAmount).isEqualTo(2_000_000L)
		assertThat(result.remainingAmount).isEqualTo(6_000_000L)
		assertThat(result.requiredMonths).isEqualTo(3)
		assertThat(result.targetAchievable).isTrue()
		assertThat(assetGoalRepository.saveCount).isEqualTo(1)
	}

	@Test
	fun `월별 저축 실적을 기록한다`() {
		val result = assetGoalServiceImpl.recordMonthlySaving(
			CreateSavingRecordCommand(
				targetMonth = "2026-07",
				amount = 700_000L,
				memo = "월 저축",
			),
		)

		assertThat(result.targetMonth).isEqualTo("2026-07")
		assertThat(result.amount).isEqualTo(700_000L)
	}

	@Test
	fun `이미 기록된 월에는 월별 저축 실적을 중복 기록할 수 없다`() {
		savingRecordRepository.save(SavingRecord("2026-07", 500_000L, null))

		assertThatThrownBy {
			assetGoalServiceImpl.recordMonthlySaving(
				CreateSavingRecordCommand(
					targetMonth = "2026-07",
					amount = 700_000L,
					memo = null,
				),
			)
		}.isInstanceOf(DuplicateSavingRecordException::class.java)
	}

	@Test
	fun `DB unique 충돌도 월별 저축 중복 예외로 변환한다`() {
		savingRecordRepository.throwDataIntegrityOnSave = true

		assertThatThrownBy {
			assetGoalServiceImpl.recordMonthlySaving(
				CreateSavingRecordCommand(
					targetMonth = "2026-07",
					amount = 700_000L,
					memo = null,
				),
			)
		}.isInstanceOf(DuplicateSavingRecordException::class.java)
	}

	@Test
	fun `유효하지 않은 월에는 저축 실적을 기록할 수 없다`() {
		assertThatThrownBy {
			assetGoalServiceImpl.recordMonthlySaving(
				CreateSavingRecordCommand(
					targetMonth = "2026-13",
					amount = 700_000L,
					memo = null,
				),
			)
		}.isInstanceOf(InvalidTargetMonthException::class.java)
	}

	@Test
	fun `월별 저축 실적은 허용된 기간의 월 범위로 조회한다`() {
		val toTargetMonth = YearMonth.now().minusMonths(1)
		val fromTargetMonth = toTargetMonth.minusMonths(2)
		savingRecordRepository.save(SavingRecord(toTargetMonth.toString(), 700_000L, null))
		savingRecordRepository.save(SavingRecord(fromTargetMonth.minusMonths(1).toString(), 300_000L, null))

		val result = assetGoalServiceImpl.getMonthlySavingRecords(3)

		assertThat(savingRecordRepository.lastRange).isEqualTo(fromTargetMonth.toString() to toTargetMonth.toString())
		assertThat(result).hasSize(1)
		assertThat(result.first().amount).isEqualTo(700_000L)
	}

	@Test
	fun `허용되지 않은 기간으로 월별 저축 실적을 조회할 수 없다`() {
		assertThatThrownBy { assetGoalServiceImpl.getMonthlySavingRecords(5) }
			.isInstanceOf(InvalidSavingPeriodException::class.java)
	}
}

private class FakeAssetGoalRepository : AssetGoalRepository {
	private val assetGoals = linkedMapOf<UUID, AssetGoal>()
	var saveCount = 0

	override fun save(assetGoal: AssetGoal): AssetGoal {
		saveCount += 1
		if (assetGoal.id == null) {
			assetGoal.assignBaseFields()
		}
		assetGoals[requireNotNull(assetGoal.id)] = assetGoal
		return assetGoal
	}

	override fun findById(id: UUID): AssetGoal? = assetGoals[id]

	override fun findAll(): List<AssetGoal> = assetGoals.values.toList()

	override fun delete(assetGoal: AssetGoal) {
		assetGoals.remove(assetGoal.id)
	}
}

private class FakeAssetGoalSavingRecordRepository : SavingRecordRepository {
	private val records = linkedMapOf<UUID, SavingRecord>()
	var lastRange: Pair<String, String>? = null
	var throwDataIntegrityOnSave = false

	override fun save(savingRecord: SavingRecord): SavingRecord {
		if (throwDataIntegrityOnSave) {
			throw DataIntegrityViolationException("duplicate target_month")
		}
		if (savingRecord.id == null) {
			savingRecord.assignBaseFields()
		}
		records[requireNotNull(savingRecord.id)] = savingRecord
		return savingRecord
	}

	override fun findById(id: UUID): SavingRecord? = records[id]

	override fun findByTargetMonth(targetMonth: String): SavingRecord? =
		records.values.firstOrNull { it.targetMonth == targetMonth }

	override fun findAllByTargetMonthBetweenOrderByTargetMonthDesc(
		fromTargetMonth: String,
		toTargetMonth: String,
	): List<SavingRecord> {
		lastRange = fromTargetMonth to toTargetMonth
		return records.values
			.filter { it.targetMonth in fromTargetMonth..toTargetMonth }
			.sortedByDescending { it.targetMonth }
	}

	override fun existsByTargetMonth(targetMonth: String): Boolean =
		records.values.any { it.targetMonth == targetMonth }

	override fun delete(savingRecord: SavingRecord) {
		records.remove(savingRecord.id)
	}
}

private class FakeAccountRepository : AccountRepository {
	var accounts: List<Account> = emptyList()

	override fun save(account: Account): Account = account

	override fun findById(id: UUID): Account? = null

	override fun findAll(): List<Account> = accounts

	override fun delete(account: Account) = Unit
}

private fun BaseEntity.assignBaseFields(id: UUID = UUID.randomUUID()) {
	val baseClass = BaseEntity::class.java
	baseClass.getDeclaredField("id").apply {
		isAccessible = true
		set(this@assignBaseFields, id)
	}
	baseClass.getDeclaredField("createdAt").apply {
		isAccessible = true
		set(this@assignBaseFields, LocalDateTime.now())
	}
	baseClass.getDeclaredField("updatedAt").apply {
		isAccessible = true
		set(this@assignBaseFields, LocalDateTime.now())
	}
}
