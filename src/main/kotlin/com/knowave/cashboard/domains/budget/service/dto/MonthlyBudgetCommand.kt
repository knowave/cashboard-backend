package com.knowave.cashboard.domains.budget.service.dto

import com.knowave.cashboard.domains.budget.entity.MonthlyBudget

data class CreateMonthlyBudgetCommand(
	val targetMonth: String,
	val monthlyBudget: Long,
	val usedAmount: Long,
) {
	fun toEntity(): MonthlyBudget = MonthlyBudget(
		targetMonth = targetMonth,
		monthlyBudget = monthlyBudget,
		usedAmount = usedAmount,
	)
}

data class UpdateMonthlyBudgetCommand(
	val targetMonth: String,
	val monthlyBudget: Long,
	val usedAmount: Long,
)

data class UpdateUsedAmountCommand(
	val usedAmount: Long,
)
