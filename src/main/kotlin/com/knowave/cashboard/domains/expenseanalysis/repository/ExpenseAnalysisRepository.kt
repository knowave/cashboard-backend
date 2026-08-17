package com.knowave.cashboard.domains.expenseanalysis.repository

import com.knowave.cashboard.domains.expenseanalysis.repository.dto.CategoryExpenseProjection
import com.knowave.cashboard.domains.expenseanalysis.repository.dto.MonthlyExpenseProjection
import java.time.LocalDate

interface ExpenseAnalysisRepository {
	fun findCategoryExpenses(start: LocalDate, end: LocalDate): List<CategoryExpenseProjection>
	fun findMonthlyExpenses(start: LocalDate, end: LocalDate): List<MonthlyExpenseProjection>
}
