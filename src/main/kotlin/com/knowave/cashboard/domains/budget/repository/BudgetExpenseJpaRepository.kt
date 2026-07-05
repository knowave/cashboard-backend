package com.knowave.cashboard.domains.budget.repository

import com.knowave.cashboard.domains.budget.entity.BudgetExpense
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface BudgetExpenseJpaRepository : JpaRepository<BudgetExpense, UUID> {
	fun findAllByMonthlyBudgetIdOrderBySpentAtDesc(monthlyBudgetId: UUID): List<BudgetExpense>
}
