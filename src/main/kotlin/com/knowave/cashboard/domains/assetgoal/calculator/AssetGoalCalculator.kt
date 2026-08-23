package com.knowave.cashboard.domains.assetgoal.calculator

import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.ceil

@Component
class AssetGoalCalculator {
	fun calculate(
		targetAmount: Long,
		currentAssetAmount: Long,
		targetDate: LocalDate,
		savingAmounts: List<Long>,
		baseDate: LocalDate = LocalDate.now(),
	): AssetGoalCalculation {
		val remainingAmount = (targetAmount - currentAssetAmount).coerceAtLeast(0)
		val achievementRate = calculateAchievementRate(currentAssetAmount, targetAmount)
		val averageMonthlySavingAmount = calculateAverageMonthlySavingAmount(savingAmounts)
		val requiredMonthlySavingAmount = calculateRequiredMonthlySavingAmount(
			remainingAmount = remainingAmount,
			targetDate = targetDate,
			baseDate = baseDate,
		)
		val expectedAchievementDate = calculateExpectedAchievementDate(
			remainingAmount = remainingAmount,
			averageMonthlySavingAmount = averageMonthlySavingAmount,
			baseDate = baseDate,
		)

		return AssetGoalCalculation(
			currentAssetAmount = currentAssetAmount,
			remainingAmount = remainingAmount,
			achievementRate = achievementRate,
			averageMonthlySavingAmount = averageMonthlySavingAmount,
			requiredMonthlySavingAmount = requiredMonthlySavingAmount,
			expectedAchievementDate = expectedAchievementDate,
			targetAchievable = calculateTargetAchievable(expectedAchievementDate, targetDate),
		)
	}

	fun calculateRemainingAmount(currentAssetAmount: Long, targetAmount: Long): Long =
		(targetAmount - currentAssetAmount).coerceAtLeast(0)

	fun calculateRequiredMonths(remainingAmount: Long, monthlySavingAmount: Long): Int {
		if (remainingAmount <= 0L) {
			return 0
		}
		if (monthlySavingAmount <= 0L) {
			return 0
		}
		return ceil(remainingAmount.toDouble() / monthlySavingAmount.toDouble()).toInt()
	}

	fun calculateExpectedAchievementDate(
		remainingAmount: Long,
		averageMonthlySavingAmount: Long,
		baseDate: LocalDate,
	): LocalDate? {
		if (remainingAmount <= 0L) {
			return baseDate
		}
		if (averageMonthlySavingAmount <= 0L) {
			return null
		}
		return baseDate.plusMonths(calculateRequiredMonths(remainingAmount, averageMonthlySavingAmount).toLong())
	}

	fun calculateTargetAchievable(expectedAchievementDate: LocalDate?, targetDate: LocalDate): Boolean =
		expectedAchievementDate != null && !expectedAchievementDate.isAfter(targetDate)

	private fun calculateAchievementRate(currentAssetAmount: Long, targetAmount: Long): Double {
		if (targetAmount <= 0L) {
			return 100.0
		}
		val rate = currentAssetAmount.toDouble() / targetAmount.toDouble() * 100.0
		return rate.coerceAtMost(100.0)
	}

	private fun calculateAverageMonthlySavingAmount(savingAmounts: List<Long>): Long {
		if (savingAmounts.isEmpty()) {
			return 0L
		}
		return savingAmounts.sum() / savingAmounts.size
	}

	private fun calculateRequiredMonthlySavingAmount(
		remainingAmount: Long,
		targetDate: LocalDate,
		baseDate: LocalDate,
	): Long {
		if (remainingAmount <= 0L) {
			return 0L
		}
		val remainingMonths = ChronoUnit.MONTHS.between(
			baseDate.withDayOfMonth(1),
			targetDate.withDayOfMonth(1),
		).coerceAtLeast(1)
		return ceil(remainingAmount.toDouble() / remainingMonths.toDouble()).toLong()
	}
}

data class AssetGoalCalculation(
	val currentAssetAmount: Long,
	val remainingAmount: Long,
	val achievementRate: Double,
	val averageMonthlySavingAmount: Long,
	val requiredMonthlySavingAmount: Long,
	val expectedAchievementDate: LocalDate?,
	val targetAchievable: Boolean,
)
