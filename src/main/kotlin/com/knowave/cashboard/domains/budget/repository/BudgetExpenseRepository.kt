package com.knowave.cashboard.domains.budget.repository

import com.knowave.cashboard.domains.budget.entity.BudgetExpense
import java.util.UUID

interface BudgetExpenseRepository {
	fun save(budgetExpense: BudgetExpense): BudgetExpense
	fun findById(id: UUID): BudgetExpense?
	fun findAllByMonthlyBudgetIdOrderBySpentAtDesc(monthlyBudgetId: UUID): List<BudgetExpense>
	fun delete(budgetExpense: BudgetExpense)
}
