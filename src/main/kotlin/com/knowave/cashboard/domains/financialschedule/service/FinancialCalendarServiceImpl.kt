package com.knowave.cashboard.domains.financialschedule.service

import com.knowave.cashboard.common.exception.ProjectionRangeExceededException
import com.knowave.cashboard.domains.financialschedule.calculator.CashFlowProjection
import com.knowave.cashboard.domains.financialschedule.calculator.FinancialCashFlowCalculator
import com.knowave.cashboard.domains.financialschedule.calculator.ScheduleOccurrence
import com.knowave.cashboard.domains.financialschedule.context.LiquidityBalanceProvider
import com.knowave.cashboard.domains.financialschedule.service.dto.FinancialCalendarProjectionResult
import com.knowave.cashboard.domains.financialschedule.service.dto.FinancialCalendarResult
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
@Transactional(readOnly = true)
class FinancialCalendarServiceImpl(
	private val occurrenceSource: CalendarOccurrenceSource,
	private val liquidityBalanceProvider: LiquidityBalanceProvider,
	private val clock: Clock,
) : FinancialCalendarService {
	private val calculator = FinancialCashFlowCalculator()

	override fun getCalendar(userId: UUID, year: Int, month: Int): FinancialCalendarResult {
		val targetMonth = YearMonth.of(year, month)
		val baseDate = LocalDate.now(clock)
		val currentMonth = YearMonth.from(baseDate)
		val distance = ChronoUnit.MONTHS.between(currentMonth, targetMonth)
		if (distance > MAX_PROJECTION_MONTHS) {
			throw ProjectionRangeExceededException(distance)
		}

		val monthStart = targetMonth.atDay(1)
		val monthEnd = targetMonth.atEndOfMonth()
		val items = occurrenceSource.findOccurrences(userId, monthStart, monthEnd)
		val summary = calculator.summarize(items)
		val projection = when {
			targetMonth < currentMonth -> null
			targetMonth == currentMonth -> currentProjection(userId, baseDate, items)
			else -> futureProjection(userId, baseDate, monthStart, items)
		}

		return FinancialCalendarResult(year, month, items, summary, projection)
	}

	private fun currentProjection(
		userId: UUID,
		baseDate: LocalDate,
		items: List<ScheduleOccurrence>,
	): FinancialCalendarProjectionResult {
		val startBalance = liquidityBalanceProvider.getCurrentLiquidBalance(userId)
		val projection = calculator.project(
			projectionStartDate = baseDate.plusDays(1),
			projectionStartBalance = startBalance,
			occurrences = items.filter { it.date.isAfter(baseDate) },
		)
		return projection.toResult(baseDate)
	}

	private fun futureProjection(
		userId: UUID,
		baseDate: LocalDate,
		monthStart: LocalDate,
		items: List<ScheduleOccurrence>,
	): FinancialCalendarProjectionResult {
		val currentBalance = liquidityBalanceProvider.getCurrentLiquidBalance(userId)
		val bridgeStart = baseDate.plusDays(1)
		val bridgeEnd = monthStart.minusDays(1)
		val bridgeItems = if (bridgeStart.isAfter(bridgeEnd)) {
			emptyList()
		} else {
			occurrenceSource.findOccurrences(userId, bridgeStart, bridgeEnd)
		}
		val startBalance = calculator.calculateClosingBalance(currentBalance, bridgeItems)
		return calculator.project(monthStart, startBalance, items).toResult(baseDate)
	}

	private fun CashFlowProjection.toResult(baseDate: LocalDate) = FinancialCalendarProjectionResult(
		baseDate = baseDate,
		projectionStartDate = projectionStartDate,
		projectionStartBalance = projectionStartBalance,
		projectedIncomeAmount = summary.incomeAmount,
		projectedExpenseAmount = summary.expenseAmount,
		projectedNetCashFlow = summary.netCashFlow,
		expectedClosingBalance = expectedClosingBalance,
		dailyBalances = dailyBalances,
	)

	private companion object {
		const val MAX_PROJECTION_MONTHS = 600L
	}
}
