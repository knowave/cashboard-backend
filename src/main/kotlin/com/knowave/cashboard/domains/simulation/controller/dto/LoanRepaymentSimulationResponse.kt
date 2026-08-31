package com.knowave.cashboard.domains.simulation.controller.dto

import com.knowave.cashboard.domains.simulation.calculator.LoanRepaymentDifference
import com.knowave.cashboard.domains.simulation.calculator.LoanRepaymentSchedule
import com.knowave.cashboard.domains.simulation.policy.LiquidityAssessment
import com.knowave.cashboard.domains.simulation.service.dto.LoanRepaymentSimulationResult
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

data class LoanRepaymentSimulationResponse(
	val baseDate: LocalDate,
	val loanId: UUID,
	val requestedPrepaymentAmount: Long,
	val appliedPrepaymentAmount: Long,
	val prepaymentAmountAdjusted: Boolean,
	val liquidity: LoanRepaymentLiquidityResponse,
	val current: LoanRepaymentScheduleResponse,
	val simulated: LoanRepaymentScheduleResponse,
	val difference: LoanRepaymentDifferenceResponse,
)

data class LoanRepaymentLiquidityResponse(
	val liquidAssetAmount: Long,
	val emergencyAssetAmount: Long,
	val cashEquivalentAssetAmount: Long,
	val averageMonthlyExpenseAmount: Long,
	val expenseHistoryMonthCount: Int,
	val emergencyFundBasis: String,
	val coverageMonths: Int,
	val recommendedEmergencyFundAmount: Long,
	val availableRepaymentAmount: Long,
	val safeRepaymentLimit: Long,
	val remainingLiquidAssetAmount: Long,
	val remainingCashEquivalentAssetAmount: Long,
	val safe: Boolean,
)

data class LoanRepaymentScheduleResponse(
	val remainingPrincipalAmount: Long,
	val monthlyPaymentAmount: Long,
	val remainingMonths: Int,
	val estimatedTotalInterestAmount: Long,
	val estimatedTotalPaymentAmount: Long,
	val estimatedPayoffMonth: YearMonth,
)

data class LoanRepaymentDifferenceResponse(
	val savedInterestAmount: Long,
	val reducedMonths: Int,
)

fun LoanRepaymentSimulationResult.toResponse() = LoanRepaymentSimulationResponse(
	baseDate = baseDate,
	loanId = loanId,
	requestedPrepaymentAmount = requestedPrepaymentAmount,
	appliedPrepaymentAmount = appliedPrepaymentAmount,
	prepaymentAmountAdjusted = prepaymentAmountAdjusted,
	liquidity = liquidity.toResponse(),
	current = current.toResponse(),
	simulated = simulated.toResponse(),
	difference = difference.toResponse(),
)

private fun LiquidityAssessment.toResponse() = LoanRepaymentLiquidityResponse(
	liquidAssetAmount = liquidAssetAmount,
	emergencyAssetAmount = emergencyAssetAmount,
	cashEquivalentAssetAmount = cashEquivalentAssetAmount,
	averageMonthlyExpenseAmount = averageMonthlyExpenseAmount,
	expenseHistoryMonthCount = expenseHistoryMonthCount,
	emergencyFundBasis = emergencyFundBasis.name,
	coverageMonths = coverageMonths,
	recommendedEmergencyFundAmount = recommendedEmergencyFundAmount,
	availableRepaymentAmount = availableRepaymentAmount,
	safeRepaymentLimit = safeRepaymentLimit,
	remainingLiquidAssetAmount = remainingLiquidAssetAmount,
	remainingCashEquivalentAssetAmount = remainingCashEquivalentAssetAmount,
	safe = safe,
)

private fun LoanRepaymentSchedule.toResponse() = LoanRepaymentScheduleResponse(
	remainingPrincipalAmount = remainingPrincipalAmount,
	monthlyPaymentAmount = monthlyPaymentAmount,
	remainingMonths = remainingMonths,
	estimatedTotalInterestAmount = estimatedTotalInterestAmount,
	estimatedTotalPaymentAmount = estimatedTotalPaymentAmount,
	estimatedPayoffMonth = estimatedPayoffMonth,
)

private fun LoanRepaymentDifference.toResponse() = LoanRepaymentDifferenceResponse(
	savedInterestAmount = savedInterestAmount,
	reducedMonths = reducedMonths,
)
