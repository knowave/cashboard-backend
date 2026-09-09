package com.knowave.cashboard.domains.assetgoal.service

import com.knowave.cashboard.common.exception.AssetGoalNotFoundException
import com.knowave.cashboard.common.exception.DuplicateSavingRecordException
import com.knowave.cashboard.common.exception.InvalidTargetMonthException
import com.knowave.cashboard.common.exception.SavingRecordNotFoundException
import com.knowave.cashboard.domains.account.repository.AccountRepository
import com.knowave.cashboard.domains.account.repository.AccountBalanceLockRepository
import com.knowave.cashboard.domains.assetgoal.calculator.AssetGoalCalculation
import com.knowave.cashboard.domains.assetgoal.calculator.AssetGoalCalculator
import com.knowave.cashboard.domains.assetgoal.entity.AssetGoal
import com.knowave.cashboard.domains.assetgoal.entity.SavingPeriod
import com.knowave.cashboard.domains.assetgoal.entity.SavingRecord
import com.knowave.cashboard.domains.assetgoal.event.AssetGoalChangedEvent
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
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
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
	private val accountBalanceLockRepository: AccountBalanceLockRepository,
	private val eventPublisher: ApplicationEventPublisher,
	private val clock: Clock,
) : AssetGoalService {
	@Transactional
	override fun createAssetGoal(userId: UUID, command: CreateAssetGoalCommand): AssetGoalDetailResult {
		accountBalanceLockRepository.acquireTotalAssetLock(userId)
		val assetGoal = assetGoalRepository.save(command.toEntity(userId))
		val currentAssetAmount = calculateCurrentAssetAmount(userId)
		eventPublisher.publishEvent(
			AssetGoalChangedEvent(
				userId = userId,
				goalId = requireNotNull(assetGoal.id),
				goalName = assetGoal.name,
				previousTargetAmount = assetGoal.targetAmount,
				currentTargetAmount = assetGoal.targetAmount,
				previousAssetAmount = 0L,
				currentAssetAmount = currentAssetAmount,
				occurredAt = clock.instant(),
			),
		)
		return assetGoal.toDetailResult(userId, DEFAULT_SAVING_PERIOD_MONTHS)
	}

	override fun getAssetGoalSummaries(userId: UUID, savingPeriodMonths: Int): List<AssetGoalSummaryResult> {
		SavingPeriod.from(savingPeriodMonths)
		val currentAssetAmount = calculateCurrentAssetAmount(userId)

		return assetGoalRepository.findAllByUserId(userId).map { assetGoal ->
			val calculation = assetGoalCalculator.calculate(
				targetAmount = assetGoal.targetAmount,
				currentAssetAmount = currentAssetAmount,
				targetDate = assetGoal.targetDate,
				savingAmounts = emptyList(),
			)
			assetGoal.toSummaryResult(calculation)
		}
	}

	override fun getAssetGoalDetail(userId: UUID, assetGoalId: UUID, savingPeriodMonths: Int): AssetGoalDetailResult {
		val assetGoal = assetGoalRepository.findByIdAndUserId(assetGoalId, userId)
			?: throw AssetGoalNotFoundException(assetGoalId)
		return assetGoal.toDetailResult(userId, savingPeriodMonths)
	}

	@Transactional
	override fun updateAssetGoal(
		userId: UUID,
		assetGoalId: UUID,
		command: UpdateAssetGoalCommand,
	): AssetGoalDetailResult {
		accountBalanceLockRepository.acquireTotalAssetLock(userId)
		val assetGoal = assetGoalRepository.findByIdAndUserId(assetGoalId, userId)
			?: throw AssetGoalNotFoundException(assetGoalId)

		val previousTargetAmount = assetGoal.targetAmount
		val updatedAssetGoal = AssetGoal.applyUpdate(assetGoal, command)
		val saved = assetGoalRepository.save(updatedAssetGoal)
		val currentAssetAmount = calculateCurrentAssetAmount(userId)
		eventPublisher.publishEvent(
			AssetGoalChangedEvent(
				userId = userId,
				goalId = requireNotNull(saved.id),
				goalName = saved.name,
				previousTargetAmount = previousTargetAmount,
				currentTargetAmount = saved.targetAmount,
				previousAssetAmount = currentAssetAmount,
				currentAssetAmount = currentAssetAmount,
				occurredAt = clock.instant(),
			),
		)
		return saved.toDetailResult(userId, DEFAULT_SAVING_PERIOD_MONTHS)
	}

	@Transactional
	override fun deleteAssetGoal(userId: UUID, assetGoalId: UUID): Boolean {
		accountBalanceLockRepository.acquireTotalAssetLock(userId)
		val assetGoal = assetGoalRepository.findByIdAndUserId(assetGoalId, userId)
			?: throw AssetGoalNotFoundException(assetGoalId)
		assetGoalRepository.delete(assetGoal)
		return true
	}

	override fun simulateAssetGoal(
		userId: UUID,
		assetGoalId: UUID,
		command: AssetGoalSimulationCommand,
	): AssetGoalSimulationResult {
		val assetGoal = assetGoalRepository.findByIdAndUserId(assetGoalId, userId)
			?: throw AssetGoalNotFoundException(assetGoalId)
		val currentAssetAmount = calculateCurrentAssetAmount(userId)
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

	override fun recordMonthlySaving(userId: UUID, command: CreateSavingRecordCommand): SavingRecordResult {
		validateTargetMonth(command.targetMonth)
		if (savingRecordRepository.existsByTargetMonthAndUserId(command.targetMonth, userId)) {
			throw DuplicateSavingRecordException(command.targetMonth)
		}

		return try {
			savingRecordRepository.save(command.toEntity(userId)).toResult()
		} catch (exception: DataIntegrityViolationException) {
			throw DuplicateSavingRecordException(command.targetMonth)
		}
	}

	override fun getMonthlySavingRecords(userId: UUID, periodMonths: Int): List<SavingRecordResult> =
		findSavingRecords(userId, periodMonths).map { it.toResult() }

	override fun getMonthlySavingRecord(userId: UUID, targetMonth: String): SavingRecordResult {
		validateTargetMonth(targetMonth)
		return savingRecordRepository.findByTargetMonthAndUserId(targetMonth, userId)?.toResult()
			?: throw SavingRecordNotFoundException(targetMonth)
	}

	override fun updateMonthlySaving(userId: UUID, id: UUID, command: UpdateSavingRecordCommand): SavingRecordResult {
		validateTargetMonth(command.targetMonth)
		val savingRecord = savingRecordRepository.findByIdAndUserId(id, userId)
			?: throw SavingRecordNotFoundException(id)
		val existingRecord = savingRecordRepository.findByTargetMonthAndUserId(command.targetMonth, userId)

		if (existingRecord != null && existingRecord.id != id) {
			throw DuplicateSavingRecordException(command.targetMonth)
		}

		return try {
			savingRecordRepository.save(SavingRecord.applyUpdate(savingRecord, command)).toResult()
		} catch (exception: DataIntegrityViolationException) {
			throw DuplicateSavingRecordException(command.targetMonth)
		}
	}

	override fun deleteMonthlySaving(userId: UUID, id: UUID): Boolean {
		val savingRecord = savingRecordRepository.findByIdAndUserId(id, userId)
			?: throw SavingRecordNotFoundException(id)
		savingRecordRepository.delete(savingRecord)
		return true
	}

	private fun AssetGoal.toDetailResult(userId: UUID, savingPeriodMonths: Int): AssetGoalDetailResult {
		val savingAmounts = findSavingAmounts(userId, savingPeriodMonths)
		val calculation = assetGoalCalculator.calculate(
			targetAmount = targetAmount,
			currentAssetAmount = calculateCurrentAssetAmount(userId),
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

	private fun findSavingAmounts(userId: UUID, savingPeriodMonths: Int): List<Long> {
		return findSavingRecords(userId, savingPeriodMonths).map { it.amount }
	}

	private fun findSavingRecords(userId: UUID, savingPeriodMonths: Int): List<SavingRecord> {
		val savingPeriod = SavingPeriod.from(savingPeriodMonths)
		val toTargetMonth = YearMonth.now().minusMonths(1)
		val fromTargetMonth = toTargetMonth.minusMonths(savingPeriod.months.toLong() - 1)

		return savingRecordRepository.findAllByTargetMonthBetweenAndUserIdOrderByTargetMonthDesc(
			fromTargetMonth = fromTargetMonth.toString(),
			toTargetMonth = toTargetMonth.toString(),
			userId = userId,
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

	private fun calculateCurrentAssetAmount(userId: UUID): Long = accountRepository.findAllByUserId(userId)
		.fold(0L) { total, account -> Math.addExact(total, account.balance) }

	private companion object {
		const val DEFAULT_SAVING_PERIOD_MONTHS = 3
		val TARGET_MONTH_PATTERN = Regex("\\d{4}-\\d{2}")
	}
}
