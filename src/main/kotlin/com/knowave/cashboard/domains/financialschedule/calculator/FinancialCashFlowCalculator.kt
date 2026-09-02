package com.knowave.cashboard.domains.financialschedule.calculator

import com.knowave.cashboard.domains.financialschedule.entity.CashFlowDirection
import java.time.LocalDate

data class CashFlowSummary(
	val incomeAmount: Long,
	val expenseAmount: Long,
	val netCashFlow: Long,
)

data class DailyBalance(
	val date: LocalDate,
	val incomeAmount: Long,
	val expenseAmount: Long,
	val expectedClosingBalance: Long,
)

data class CashFlowProjection(
	val projectionStartDate: LocalDate,
	val projectionStartBalance: Long,
	val summary: CashFlowSummary,
	val expectedClosingBalance: Long,
	val dailyBalances: List<DailyBalance>,
)

class FinancialCashFlowCalculator {
	fun summarize(occurrences: List<ScheduleOccurrence>): CashFlowSummary {
		val income = exactSum(occurrences, CashFlowDirection.INCOME)
		val expense = exactSum(occurrences, CashFlowDirection.EXPENSE)
		return CashFlowSummary(income, expense, Math.subtractExact(income, expense))
	}

	fun calculateClosingBalance(openingBalance: Long, occurrences: List<ScheduleOccurrence>): Long =
		Math.addExact(openingBalance, summarize(occurrences).netCashFlow)

	fun project(
		projectionStartDate: LocalDate,
		projectionStartBalance: Long,
		occurrences: List<ScheduleOccurrence>,
	): CashFlowProjection {
		var balance = projectionStartBalance
		val dailyBalances = occurrences
			.groupBy { it.date }
			.toSortedMap()
			.map { (date, dailyOccurrences) ->
				val dailySummary = summarize(dailyOccurrences)
				balance = Math.addExact(balance, dailySummary.netCashFlow)
				DailyBalance(date, dailySummary.incomeAmount, dailySummary.expenseAmount, balance)
			}

		return CashFlowProjection(
			projectionStartDate = projectionStartDate,
			projectionStartBalance = projectionStartBalance,
			summary = summarize(occurrences),
			expectedClosingBalance = balance,
			dailyBalances = dailyBalances,
		)
	}

	private fun exactSum(occurrences: List<ScheduleOccurrence>, direction: CashFlowDirection): Long =
		occurrences
			.asSequence()
			.filter { it.direction == direction }
			.fold(0L) { total, occurrence -> Math.addExact(total, occurrence.amount) }
}
