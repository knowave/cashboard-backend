package com.knowave.cashboard.domains.expenseanalysis.repository

import com.knowave.cashboard.domains.expenseanalysis.repository.dto.CategoryExpenseProjection
import com.knowave.cashboard.domains.expenseanalysis.repository.dto.MonthlyExpenseProjection
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class ExpenseAnalysisRepositoryImpl(
	private val expenseAnalysisJpaRepository: ExpenseAnalysisJpaRepository,
) : ExpenseAnalysisRepository {
	override fun findCategoryExpenses(start: LocalDate, end: LocalDate): List<CategoryExpenseProjection> =
		expenseAnalysisJpaRepository.findCategoryExpenses(start, end)

	override fun findMonthlyExpenses(start: LocalDate, end: LocalDate): List<MonthlyExpenseProjection> =
		expenseAnalysisJpaRepository.findMonthlyExpenses(start, end)
}
