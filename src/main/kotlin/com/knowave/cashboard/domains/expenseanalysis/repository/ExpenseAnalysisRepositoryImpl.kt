package com.knowave.cashboard.domains.expenseanalysis.repository

import com.knowave.cashboard.domains.expenseanalysis.repository.dto.CategoryExpenseProjection
import com.knowave.cashboard.domains.expenseanalysis.repository.dto.MonthlyExpenseProjection
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.UUID

@Repository
class ExpenseAnalysisRepositoryImpl(
	private val expenseAnalysisJpaRepository: ExpenseAnalysisJpaRepository,
) : ExpenseAnalysisRepository {
	override fun findCategoryExpenses(userId: UUID, start: LocalDate, end: LocalDate): List<CategoryExpenseProjection> =
		expenseAnalysisJpaRepository.findCategoryExpenses(userId, start, end)

	override fun findMonthlyExpenses(userId: UUID, start: LocalDate, end: LocalDate): List<MonthlyExpenseProjection> =
		expenseAnalysisJpaRepository.findMonthlyExpenses(userId, start, end)
}
