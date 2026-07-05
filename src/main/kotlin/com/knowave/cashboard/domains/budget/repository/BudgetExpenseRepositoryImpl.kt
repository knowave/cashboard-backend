package com.knowave.cashboard.domains.budget.repository

import com.knowave.cashboard.domains.budget.entity.BudgetExpense
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class BudgetExpenseRepositoryImpl(
	private val budgetExpenseJpaRepository: BudgetExpenseJpaRepository,
) : BudgetExpenseRepository {
	override fun save(budgetExpense: BudgetExpense): BudgetExpense =
		budgetExpenseJpaRepository.save(budgetExpense)

	override fun findById(id: UUID): BudgetExpense? =
		budgetExpenseJpaRepository.findById(id).orElse(null)

	override fun findAllByMonthlyBudgetIdOrderBySpentAtDesc(monthlyBudgetId: UUID): List<BudgetExpense> =
		budgetExpenseJpaRepository.findAllByMonthlyBudgetIdOrderBySpentAtDesc(monthlyBudgetId)

	override fun delete(budgetExpense: BudgetExpense) =
		budgetExpenseJpaRepository.delete(budgetExpense)
}
