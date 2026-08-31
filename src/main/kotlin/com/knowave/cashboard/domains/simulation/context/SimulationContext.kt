package com.knowave.cashboard.domains.simulation.context

import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

data class LiquidityContext(
	val baseDate: LocalDate,
	val liquidAssetAmount: Long,
	val emergencyAssetAmount: Long,
	val averageMonthlyExpenseAmount: Long,
	val expenseHistoryMonthCount: Int,
)

data class LoanSnapshot(
	val id: UUID,
	val currentBalance: Long,
	val annualInterestRate: BigDecimal,
	val monthlyPaymentAmount: Long,
)

data class SimulationContext(
	val baseDate: LocalDate,
	val liquidAssetAmount: Long,
	val emergencyAssetAmount: Long,
	val averageMonthlyExpenseAmount: Long,
	val expenseHistoryMonthCount: Int,
	val loan: LoanSnapshot,
) {
	fun toLiquidityContext(): LiquidityContext = LiquidityContext(
		baseDate = baseDate,
		liquidAssetAmount = liquidAssetAmount,
		emergencyAssetAmount = emergencyAssetAmount,
		averageMonthlyExpenseAmount = averageMonthlyExpenseAmount,
		expenseHistoryMonthCount = expenseHistoryMonthCount,
	)
}
