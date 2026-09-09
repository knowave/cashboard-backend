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

	override fun findByIdAndUserId(id: UUID, userId: UUID): BudgetExpense? =
		budgetExpenseJpaRepository.findByIdAndUserId(id, userId)

	override fun findAllByMonthlyBudgetIdAndUserIdOrderBySpentAtDesc(
		monthlyBudgetId: UUID,
		userId: UUID,
	): List<BudgetExpense> =
		budgetExpenseJpaRepository.findAllByMonthlyBudgetIdAndUserIdOrderBySpentAtDesc(monthlyBudgetId, userId)

	override fun delete(budgetExpense: BudgetExpense) =
		budgetExpenseJpaRepository.delete(budgetExpense)
}
