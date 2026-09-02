package com.knowave.cashboard.domains.financialschedule.controller.dto

import com.knowave.cashboard.domains.financialschedule.service.dto.FinancialCalendarResult
import java.time.LocalDate
import java.util.UUID

data class FinancialCalendarResponse(
	val year: Int,
	val month: Int,
	val items: List<FinancialCalendarItemResponse>,
	val summary: FinancialCalendarSummaryResponse,
	val projection: FinancialCalendarProjectionResponse?,
)

data class FinancialCalendarItemResponse(
	val scheduleId: UUID,
	val date: LocalDate,
	val type: String,
	val title: String,
	val direction: String,
	val amount: Long,
)

data class FinancialCalendarSummaryResponse(
	val scheduledIncomeAmount: Long,
	val scheduledExpenseAmount: Long,
	val netScheduledCashFlow: Long,
)

data class FinancialCalendarProjectionResponse(
	val baseDate: LocalDate,
	val projectionStartDate: LocalDate,
	val projectionStartBalance: Long,
	val projectedIncomeAmount: Long,
	val projectedExpenseAmount: Long,
	val projectedNetCashFlow: Long,
	val expectedClosingBalance: Long,
	val dailyBalances: List<DailyBalanceResponse>,
)

data class DailyBalanceResponse(
	val date: LocalDate,
	val incomeAmount: Long,
	val expenseAmount: Long,
	val expectedClosingBalance: Long,
)

fun FinancialCalendarResult.toResponse() = FinancialCalendarResponse(
	year = year,
	month = month,
	items = items.map {
		FinancialCalendarItemResponse(
			scheduleId = it.scheduleId,
			date = it.date,
			type = it.type.name,
			title = it.title,
			direction = it.direction.name,
			amount = it.amount,
		)
	},
	summary = FinancialCalendarSummaryResponse(
		scheduledIncomeAmount = summary.incomeAmount,
		scheduledExpenseAmount = summary.expenseAmount,
		netScheduledCashFlow = summary.netCashFlow,
	),
	projection = projection?.let {
		FinancialCalendarProjectionResponse(
			baseDate = it.baseDate,
			projectionStartDate = it.projectionStartDate,
			projectionStartBalance = it.projectionStartBalance,
			projectedIncomeAmount = it.projectedIncomeAmount,
			projectedExpenseAmount = it.projectedExpenseAmount,
			projectedNetCashFlow = it.projectedNetCashFlow,
			expectedClosingBalance = it.expectedClosingBalance,
			dailyBalances = it.dailyBalances.map { balance ->
				DailyBalanceResponse(
					date = balance.date,
					incomeAmount = balance.incomeAmount,
					expenseAmount = balance.expenseAmount,
					expectedClosingBalance = balance.expectedClosingBalance,
				)
			},
		)
	},
)
