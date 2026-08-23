package com.knowave.cashboard.domains.assetgoal.service

import com.knowave.cashboard.common.exception.AssetGoalNotFoundException
import com.knowave.cashboard.common.exception.DuplicateSavingRecordException
import com.knowave.cashboard.common.exception.InvalidTargetMonthException
import com.knowave.cashboard.common.exception.SavingRecordNotFoundException
import com.knowave.cashboard.domains.account.repository.AccountRepository
import com.knowave.cashboard.domains.assetgoal.calculator.AssetGoalCalculation
import com.knowave.cashboard.domains.assetgoal.calculator.AssetGoalCalculator
import com.knowave.cashboard.domains.assetgoal.entity.AssetGoal
import com.knowave.cashboard.domains.assetgoal.entity.SavingPeriod
import com.knowave.cashboard.domains.assetgoal.entity.SavingRecord
import com.knowave.cashboard.domains.assetgoal.repository.AssetGoalRepository
import com.knowave.cashboard.domains.assetgoal.repository.SavingRecordRepository
import com.knowave.cashboard.domains.assetgoal.service.dto.AssetGoalDetailResult
import com.knowave.cashboard.domains.assetgoal.service.dto.AssetGoalSimulationCommand
import com.knowave.cashboard.domains.assetgoal.service.dto.AssetGoalSimulationResult
import com.knowave.cashboard.domains.assetgoal.service.dto.AssetGoalSummaryResult
import com.knowave.cashboard.domains.assetgoal.service.dto.CreateAssetGoalCommand
import com.knowave.cashboard.domains.assetgoal.service.dto.CreateSavingRecordCommand
import com.knowave.cashboard.domains.assetgoal.service.dto.SavingRecordResult
import com.knowave.cashboard.domains.assetgoal.service.dto.UpdateAssetGoalCommand
import com.knowave.cashboard.domains.assetgoal.service.dto.UpdateSavingRecordCommand
import com.knowave.cashboard.domains.assetgoal.service.dto.toResult
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeParseException
import java.util.UUID

@Service
class AssetGoalServiceImpl(
	private val assetGoalRepository: AssetGoalRepository,
	private val savingRecordRepository: SavingRecordRepository,
	private val accountRepository: AccountRepository,
	private val assetGoalCalculator: AssetGoalCalculator,
) : AssetGoalService {
	override fun createAssetGoal(command: CreateAssetGoalCommand): AssetGoalDetailResult {
		val assetGoal = assetGoalRepository.save(command.toEntity())
		return assetGoal.toDetailResult(DEFAULT_SAVING_PERIOD_MONTHS)
	}

	override fun getAssetGoalSummaries(savingPeriodMonths: Int): List<AssetGoalSummaryResult> {
		SavingPeriod.from(savingPeriodMonths)
		val currentAssetAmount = calculateCurrentAssetAmount()

		return assetGoalRepository.findAll().map { assetGoal ->
			val calculation = assetGoalCalculator.calculate(
				targetAmount = assetGoal.targetAmount,
				currentAssetAmount = currentAssetAmount,
				targetDate = assetGoal.targetDate,
				savingAmounts = emptyList(),
			)
			assetGoal.toSummaryResult(calculation)
		}
	}

	override fun getAssetGoalDetail(assetGoalId: UUID, savingPeriodMonths: Int): AssetGoalDetailResult {
		val assetGoal = assetGoalRepository.findById(assetGoalId)
			?: throw AssetGoalNotFoundException(assetGoalId)
		return assetGoal.toDetailResult(savingPeriodMonths)
	}

	override fun updateAssetGoal(assetGoalId: UUID, command: UpdateAssetGoalCommand): AssetGoalDetailResult {
		val assetGoal = assetGoalRepository.findById(assetGoalId)
			?: throw AssetGoalNotFoundException(assetGoalId)

		val updatedAssetGoal = AssetGoal.applyUpdate(assetGoal, command)
		return assetGoalRepository.save(updatedAssetGoal).toDetailResult(DEFAULT_SAVING_PERIOD_MONTHS)
	}

	override fun deleteAssetGoal(assetGoalId: UUID): Boolean {
		val assetGoal = assetGoalRepository.findById(assetGoalId)
			?: throw AssetGoalNotFoundException(assetGoalId)
		assetGoalRepository.delete(assetGoal)
		return true
	}

	override fun simulateAssetGoal(assetGoalId: UUID, command: AssetGoalSimulationCommand): AssetGoalSimulationResult {
		val assetGoal = assetGoalRepository.findById(assetGoalId)
			?: throw AssetGoalNotFoundException(assetGoalId)
		val currentAssetAmount = calculateCurrentAssetAmount()
		val remainingAmount = assetGoalCalculator.calculateRemainingAmount(
			currentAssetAmount = currentAssetAmount,
			targetAmount = assetGoal.targetAmount,
		)
		val requiredMonths = assetGoalCalculator.calculateRequiredMonths(
			remainingAmount = remainingAmount,
			monthlySavingAmount = command.monthlySavingAmount,
		)
		val expectedAchievementDate = assetGoalCalculator.calculateExpectedAchievementDate(
			remainingAmount = remainingAmount,
			averageMonthlySavingAmount = command.monthlySavingAmount,
			baseDate = LocalDate.now(),
		)
		val targetAchievable = assetGoalCalculator.calculateTargetAchievable(
			expectedAchievementDate = expectedAchievementDate,
			targetDate = assetGoal.targetDate,
		)

		return AssetGoalSimulationResult(
			monthlySavingAmount = command.monthlySavingAmount,
			currentAssetAmount = currentAssetAmount,
			remainingAmount = remainingAmount,
			requiredMonths = requiredMonths,
			expectedAchievementDate = expectedAchievementDate,
			targetDate = assetGoal.targetDate,
			targetAchievable = targetAchievable,
		)
	}

	override fun recordMonthlySaving(command: CreateSavingRecordCommand): SavingRecordResult {
		validateTargetMonth(command.targetMonth)
		if (savingRecordRepository.existsByTargetMonth(command.targetMonth)) {
			throw DuplicateSavingRecordException(command.targetMonth)
		}

		return try {
			savingRecordRepository.save(command.toEntity()).toResult()
		} catch (exception: DataIntegrityViolationException) {
			throw DuplicateSavingRecordException(command.targetMonth)
		}
	}

	override fun getMonthlySavingRecords(periodMonths: Int): List<SavingRecordResult> =
		findSavingRecords(periodMonths).map { it.toResult() }

	override fun getMonthlySavingRecord(targetMonth: String): SavingRecordResult {
		validateTargetMonth(targetMonth)
		return savingRecordRepository.findByTargetMonth(targetMonth)?.toResult()
			?: throw SavingRecordNotFoundException(targetMonth)
	}

	override fun updateMonthlySaving(id: UUID, command: UpdateSavingRecordCommand): SavingRecordResult {
		validateTargetMonth(command.targetMonth)
		val savingRecord = savingRecordRepository.findById(id)
			?: throw SavingRecordNotFoundException(id)
		val existingRecord = savingRecordRepository.findByTargetMonth(command.targetMonth)

		if (existingRecord != null && existingRecord.id != id) {
			throw DuplicateSavingRecordException(command.targetMonth)
		}

		return try {
			savingRecordRepository.save(SavingRecord.applyUpdate(savingRecord, command)).toResult()
		} catch (exception: DataIntegrityViolationException) {
			throw DuplicateSavingRecordException(command.targetMonth)
		}
	}

	override fun deleteMonthlySaving(id: UUID): Boolean {
		val savingRecord = savingRecordRepository.findById(id)
			?: throw SavingRecordNotFoundException(id)
		savingRecordRepository.delete(savingRecord)
		return true
	}

	private fun AssetGoal.toDetailResult(savingPeriodMonths: Int): AssetGoalDetailResult {
		val savingAmounts = findSavingAmounts(savingPeriodMonths)
		val calculation = assetGoalCalculator.calculate(
			targetAmount = targetAmount,
			currentAssetAmount = calculateCurrentAssetAmount(),
			targetDate = targetDate,
			savingAmounts = savingAmounts,
		)

		return AssetGoalDetailResult(
			id = requireNotNull(id),
			name = name,
			targetAmount = targetAmount,
			targetDate = targetDate,
			currentAssetAmount = calculation.currentAssetAmount,
			remainingAmount = calculation.remainingAmount,
			achievementRate = calculation.achievementRate,
			savingPeriodMonths = savingPeriodMonths,
			averageMonthlySavingAmount = calculation.averageMonthlySavingAmount,
			requiredMonthlySavingAmount = calculation.requiredMonthlySavingAmount,
			expectedAchievementDate = calculation.expectedAchievementDate,
			targetAchievable = calculation.targetAchievable,
			createdAt = requireNotNull(createdAt),
			updatedAt = requireNotNull(updatedAt),
		)
	}

	private fun AssetGoal.toSummaryResult(calculation: AssetGoalCalculation): AssetGoalSummaryResult =
		AssetGoalSummaryResult(
			id = requireNotNull(id),
			name = name,
			targetAmount = targetAmount,
			targetDate = targetDate,
			currentAssetAmount = calculation.currentAssetAmount,
			remainingAmount = calculation.remainingAmount,
			achievementRate = calculation.achievementRate,
			createdAt = requireNotNull(createdAt),
			updatedAt = requireNotNull(updatedAt),
		)

	private fun findSavingAmounts(savingPeriodMonths: Int): List<Long> {
		return findSavingRecords(savingPeriodMonths).map { it.amount }
	}

	private fun findSavingRecords(savingPeriodMonths: Int): List<SavingRecord> {
		val savingPeriod = SavingPeriod.from(savingPeriodMonths)
		val toTargetMonth = YearMonth.now().minusMonths(1)
		val fromTargetMonth = toTargetMonth.minusMonths(savingPeriod.months.toLong() - 1)

		return savingRecordRepository.findAllByTargetMonthBetweenOrderByTargetMonthDesc(
			fromTargetMonth = fromTargetMonth.toString(),
			toTargetMonth = toTargetMonth.toString(),
		)
	}

	private fun validateTargetMonth(targetMonth: String) {
		if (!TARGET_MONTH_PATTERN.matches(targetMonth)) {
			throw InvalidTargetMonthException(targetMonth)
		}

		try {
			YearMonth.parse(targetMonth)
		} catch (exception: DateTimeParseException) {
			throw InvalidTargetMonthException(targetMonth)
		}
	}

	private fun calculateCurrentAssetAmount(): Long = accountRepository.findAll().sumOf { it.balance }

	private companion object {
		const val DEFAULT_SAVING_PERIOD_MONTHS = 3
		val TARGET_MONTH_PATTERN = Regex("\\d{4}-\\d{2}")
	}
}
