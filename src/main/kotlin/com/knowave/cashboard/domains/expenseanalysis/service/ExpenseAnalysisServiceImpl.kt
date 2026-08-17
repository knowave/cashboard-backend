package com.knowave.cashboard.domains.expenseanalysis.service

import com.knowave.cashboard.domains.expenseanalysis.repository.ExpenseAnalysisRepository
import com.knowave.cashboard.domains.expenseanalysis.service.dto.CategoryExpenseResult
import com.knowave.cashboard.domains.expenseanalysis.service.dto.ExpenseAnalysisResult
import com.knowave.cashboard.domains.expenseanalysis.service.dto.ExpenseComparisonResult
import com.knowave.cashboard.domains.expenseanalysis.service.dto.MonthlyExpenseResult
import com.knowave.cashboard.domains.expenseanalysis.service.dto.PeriodResult
import com.knowave.cashboard.domains.expenseanalysis.service.dto.RecentAverageResult
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.YearMonth

@Service
@Transactional(readOnly = true)
class ExpenseAnalysisServiceImpl(
	private val expenseAnalysisRepository: ExpenseAnalysisRepository,
) : ExpenseAnalysisService {

	override fun getAnalysis(year: Int, month: Int): ExpenseAnalysisResult {
		val yearMonth = YearMonth.of(year, month)

		val categoryExpenses = expenseAnalysisRepository.findCategoryExpenses(
			yearMonth.atDay(1), yearMonth.plusMonths(1).atDay(1),
		)
		val totalExpense = categoryExpenses.sumOf { it.amount }
		val categories = categoryExpenses.map {
			CategoryExpenseResult(
				category = it.category,
				amount = it.amount,
				ratio = if (totalExpense == 0L) 0.0 else roundToTwoDecimals(it.amount * 100.0 / totalExpense),
			)
		}

		val windowStart = yearMonth.minusMonths(maxOf(TREND_MONTHS - 1, RECENT_AVERAGE_MONTHS))
		val monthly = expenseAnalysisRepository.findMonthlyExpenses(
			windowStart.atDay(1), yearMonth.plusMonths(1).atDay(1),
		).associateBy { YearMonth.parse(it.yearMonth) }

		val trendWindowStart = yearMonth.minusMonths(TREND_MONTHS - 1)
		val trend = monthly.entries
			.filter { it.key >= trendWindowStart }
			.sortedBy { it.key }
			.map { (ym, projection) ->
				MonthlyExpenseResult(
					yearMonth = ym.toString(),
					amount = if (ym == yearMonth) totalExpense else projection.amount,
				)
			}

		val previousYearMonth = yearMonth.minusMonths(1)
		val previousAmount = monthly[previousYearMonth]?.amount ?: 0L
		val differenceAmount = totalExpense - previousAmount
		val differenceRate = if (previousAmount == 0L) null else roundToTwoDecimals(differenceAmount * 100.0 / previousAmount)

		val recentMonths = (1..RECENT_AVERAGE_MONTHS).map { yearMonth.minusMonths(it) }
		val recentAmounts = recentMonths.mapNotNull { monthly[it]?.amount }
		val recentAverage = RecentAverageResult(
			amount = if (recentAmounts.isEmpty()) 0L else recentAmounts.sum() / recentAmounts.size,
			months = recentAmounts.size,
		)

		return ExpenseAnalysisResult(
			period = PeriodResult(year, month),
			totalExpense = totalExpense,
			previousMonthComparison = ExpenseComparisonResult(previousAmount, differenceAmount, differenceRate),
			recentAverage = recentAverage,
			categories = categories,
			trend = trend,
		)
	}

	private fun roundToTwoDecimals(value: Double): Double =
		BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).toDouble()

	private companion object {
		const val TREND_MONTHS = 6L
		const val RECENT_AVERAGE_MONTHS = 3L
	}
}
