package com.knowave.cashboard.domains.budget.service.dto

import com.knowave.cashboard.domains.budget.entity.BudgetExpense
import com.knowave.cashboard.domains.budget.entity.MonthlyBudget
import java.time.LocalDate

data class CreateBudgetExpenseCommand(
	val amount: Long,
	val category: String?,
	val memo: String?,
	val spentAt: LocalDate,
) {
	fun toEntity(monthlyBudget: MonthlyBudget): BudgetExpense = BudgetExpense(
		monthlyBudget = monthlyBudget,
		amount = amount,
		category = category,
		memo = memo,
		spentAt = spentAt,
	)
}
