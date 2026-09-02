package com.knowave.cashboard.domains.financialschedule.service.dto

import com.knowave.cashboard.domains.financialschedule.calculator.CashFlowSummary
import com.knowave.cashboard.domains.financialschedule.calculator.DailyBalance
import com.knowave.cashboard.domains.financialschedule.calculator.ScheduleOccurrence
import java.time.LocalDate

data class FinancialCalendarResult(
	val year: Int,
	val month: Int,
	val items: List<ScheduleOccurrence>,
	val summary: CashFlowSummary,
	val projection: FinancialCalendarProjectionResult?,
)

data class FinancialCalendarProjectionResult(
	val baseDate: LocalDate,
	val projectionStartDate: LocalDate,
	val projectionStartBalance: Long,
	val projectedIncomeAmount: Long,
	val projectedExpenseAmount: Long,
	val projectedNetCashFlow: Long,
	val expectedClosingBalance: Long,
	val dailyBalances: List<DailyBalance>,
)
