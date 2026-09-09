package com.knowave.cashboard.domains.budget.repository

import com.knowave.cashboard.domains.budget.entity.BudgetExpense
import java.util.UUID

interface BudgetExpenseRepository {
	fun save(budgetExpense: BudgetExpense): BudgetExpense
	fun findByIdAndUserId(id: UUID, userId: UUID): BudgetExpense?
	fun findAllByMonthlyBudgetIdAndUserIdOrderBySpentAtDesc(monthlyBudgetId: UUID, userId: UUID): List<BudgetExpense>
	fun delete(budgetExpense: BudgetExpense)
}
