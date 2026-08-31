package com.knowave.cashboard.domains.simulation.policy

import com.knowave.cashboard.domains.simulation.context.LiquidityContext
import org.springframework.stereotype.Component

enum class EmergencyFundBasis {
	RECENT_EXPENSE_AVERAGE,
	FALLBACK_POLICY,
	LEGACY_REQUEST,
}

data class EmergencyFundRecommendation(
	val amount: Long,
	val basis: EmergencyFundBasis,
	val coverageMonths: Int,
)

data class LiquidityAssessment(
	val liquidAssetAmount: Long,
	val emergencyAssetAmount: Long,
	val cashEquivalentAssetAmount: Long,
	val averageMonthlyExpenseAmount: Long,
	val expenseHistoryMonthCount: Int,
	val emergencyFundBasis: EmergencyFundBasis,
	val coverageMonths: Int,
	val recommendedEmergencyFundAmount: Long,
	val availableRepaymentAmount: Long,
	val safeRepaymentLimit: Long,
	val requestedPrepaymentAmount: Long,
	val appliedPrepaymentAmount: Long,
	val remainingLiquidAssetAmount: Long,
	val remainingCashEquivalentAssetAmount: Long,
	val safe: Boolean,
)

@Component
class EmergencyFundPolicy {
	fun recommend(context: LiquidityContext): EmergencyFundRecommendation =
		if (context.expenseHistoryMonthCount > 0) {
			EmergencyFundRecommendation(
				amount = context.averageMonthlyExpenseAmount * COVERAGE_MONTHS,
				basis = EmergencyFundBasis.RECENT_EXPENSE_AVERAGE,
				coverageMonths = COVERAGE_MONTHS,
			)
		} else {
			EmergencyFundRecommendation(
				amount = NO_HISTORY_FALLBACK_AMOUNT,
				basis = EmergencyFundBasis.FALLBACK_POLICY,
				coverageMonths = COVERAGE_MONTHS,
			)
		}

	fun legacyRecommendation(amount: Long): EmergencyFundRecommendation = EmergencyFundRecommendation(
		amount = amount,
		basis = EmergencyFundBasis.LEGACY_REQUEST,
		coverageMonths = 0,
	)

	fun assess(
		context: LiquidityContext,
		recommendation: EmergencyFundRecommendation,
		requestedPrepaymentAmount: Long,
		maximumRepaymentAmount: Long,
	): LiquidityAssessment {
		require(requestedPrepaymentAmount >= 0L)
		require(maximumRepaymentAmount >= 0L)
		val cashEquivalent = context.liquidAssetAmount + context.emergencyAssetAmount
		val safeLimit = minOf(
			context.liquidAssetAmount,
			cashEquivalent - recommendation.amount,
		).coerceAtLeast(0L)
		val applied = minOf(requestedPrepaymentAmount, safeLimit, maximumRepaymentAmount)
		val remainingLiquid = context.liquidAssetAmount - applied
		val remainingCashEquivalent = cashEquivalent - applied

		return LiquidityAssessment(
			liquidAssetAmount = context.liquidAssetAmount,
			emergencyAssetAmount = context.emergencyAssetAmount,
			cashEquivalentAssetAmount = cashEquivalent,
			averageMonthlyExpenseAmount = context.averageMonthlyExpenseAmount,
			expenseHistoryMonthCount = context.expenseHistoryMonthCount,
			emergencyFundBasis = recommendation.basis,
			coverageMonths = recommendation.coverageMonths,
			recommendedEmergencyFundAmount = recommendation.amount,
			availableRepaymentAmount = context.liquidAssetAmount,
			safeRepaymentLimit = safeLimit,
			requestedPrepaymentAmount = requestedPrepaymentAmount,
			appliedPrepaymentAmount = applied,
			remainingLiquidAssetAmount = remainingLiquid,
			remainingCashEquivalentAssetAmount = remainingCashEquivalent,
			safe = remainingCashEquivalent >= recommendation.amount,
		)
	}

	private companion object {
		const val COVERAGE_MONTHS = 3
		const val NO_HISTORY_FALLBACK_AMOUNT = 5_000_000L
	}
}
