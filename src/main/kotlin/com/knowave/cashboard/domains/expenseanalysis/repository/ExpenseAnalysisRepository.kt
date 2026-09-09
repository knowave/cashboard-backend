package com.knowave.cashboard.domains.expenseanalysis.repository

import com.knowave.cashboard.domains.expenseanalysis.repository.dto.CategoryExpenseProjection
import com.knowave.cashboard.domains.expenseanalysis.repository.dto.MonthlyExpenseProjection
import java.time.LocalDate
import java.util.UUID

interface ExpenseAnalysisRepository {
	fun findCategoryExpenses(userId: UUID, start: LocalDate, end: LocalDate): List<CategoryExpenseProjection>
	fun findMonthlyExpenses(userId: UUID, start: LocalDate, end: LocalDate): List<MonthlyExpenseProjection>
}
