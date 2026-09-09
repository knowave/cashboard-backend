package com.knowave.cashboard.domains.notification.report

import com.knowave.cashboard.domains.budget.repository.MonthlyBudgetRepository
import com.knowave.cashboard.domains.expenseanalysis.repository.ExpenseAnalysisRepository
import com.knowave.cashboard.domains.expenseanalysis.repository.dto.CategoryExpenseProjection
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

@Component
class WeeklyReportProvider(
	private val expenseRepository: ExpenseAnalysisRepository,
	private val monthlyBudgetRepository: MonthlyBudgetRepository,
) {
	fun generate(userId: UUID, runDate: LocalDate): WeeklyReportSummary {
		val weekStart = runDate.minusWeeks(1)
		val currentExpenses = expenseRepository.findCategoryExpenses(userId, weekStart, runDate)
		val previousExpenses = expenseRepository.findCategoryExpenses(userId, weekStart.minusWeeks(1), weekStart)
		val totalExpense = currentExpenses.sumOf { it.amount }
		val previousTotal = previousExpenses.sumOf { it.amount }
		val differenceRate = if (previousTotal == 0L) null else (totalExpense - previousTotal) * 100.0 / previousTotal
		val topCategory = currentExpenses.sortedWith(
			compareByDescending<CategoryExpenseProjection> { it.amount }.thenBy { it.category },
		).firstOrNull()?.category
		val budget = monthlyBudgetRepository.findByTargetMonthAndUserId(YearMonth.from(runDate).toString(), userId)

		return WeeklyReportSummary(
			weekStart = weekStart,
			totalExpense = totalExpense,
			previousWeekDifferenceRate = differenceRate,
			topCategory = topCategory,
			remainingBudget = budget?.let { it.monthlyBudget - it.usedAmount },
		)
	}
}

data class WeeklyReportSummary(
	val weekStart: LocalDate,
	val totalExpense: Long,
	val previousWeekDifferenceRate: Double?,
	val topCategory: String?,
	val remainingBudget: Long?,
) {
	fun toMessage(): String {
		if (totalExpense == 0L) return "지난주에는 지출이 없었어요."

		return buildList {
			add("지난주 총 지출은 ${totalExpense}원이에요")
			previousWeekDifferenceRate?.let { add("전주 대비 ${it}% 변화했어요") }
			topCategory?.let { add("최다 지출 카테고리는 ${it}예요") }
			remainingBudget?.let { add("이번 달 남은 예산은 ${it}원이에요") }
		}.joinToString(", ") + "."
	}
}
