package com.knowave.cashboard.domains.expenseanalysis.repository

import com.knowave.cashboard.domains.budget.entity.BudgetExpense
import com.knowave.cashboard.domains.expenseanalysis.repository.dto.CategoryExpenseProjection
import com.knowave.cashboard.domains.expenseanalysis.repository.dto.MonthlyExpenseProjection
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.util.UUID

interface ExpenseAnalysisJpaRepository : Repository<BudgetExpense, UUID> {

	@Query(
		value = """
			SELECT COALESCE(category, 'UNCATEGORIZED') AS category, CAST(SUM(amount) AS BIGINT) AS amount
			FROM budget_expenses
			WHERE spent_at >= :start AND spent_at < :end
			GROUP BY COALESCE(category, 'UNCATEGORIZED')
			ORDER BY SUM(amount) DESC, 1 ASC
		""",
		nativeQuery = true,
	)
	fun findCategoryExpenses(@Param("start") start: LocalDate, @Param("end") end: LocalDate): List<CategoryExpenseProjection>

	@Query(
		value = """
			SELECT TO_CHAR(spent_at, 'YYYY-MM') AS "yearMonth", CAST(SUM(amount) AS BIGINT) AS amount
			FROM budget_expenses
			WHERE spent_at >= :start AND spent_at < :end
			GROUP BY TO_CHAR(spent_at, 'YYYY-MM')
			ORDER BY 1
		""",
		nativeQuery = true,
	)
	fun findMonthlyExpenses(@Param("start") start: LocalDate, @Param("end") end: LocalDate): List<MonthlyExpenseProjection>
}
