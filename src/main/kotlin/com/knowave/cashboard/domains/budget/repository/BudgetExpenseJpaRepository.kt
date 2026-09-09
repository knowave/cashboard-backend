package com.knowave.cashboard.domains.budget.repository

import com.knowave.cashboard.domains.budget.entity.BudgetExpense
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface BudgetExpenseJpaRepository : JpaRepository<BudgetExpense, UUID> {
	fun findByIdAndUserId(id: UUID, userId: UUID): BudgetExpense?
	fun findAllByMonthlyBudgetIdAndUserIdOrderBySpentAtDesc(monthlyBudgetId: UUID, userId: UUID): List<BudgetExpense>
}
