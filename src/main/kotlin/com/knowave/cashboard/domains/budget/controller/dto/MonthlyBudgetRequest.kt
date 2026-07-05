package com.knowave.cashboard.domains.budget.controller.dto

import com.knowave.cashboard.domains.budget.service.dto.CreateMonthlyBudgetCommand
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.PositiveOrZero

data class MonthlyBudgetRequest(
	@field:Pattern(regexp = "\\d{4}-\\d{2}", message = "targetMonth must be yyyy-MM.")
	val targetMonth: String,

	@field:PositiveOrZero(message = "monthlyBudget must be greater than or equal to 0.")
	val monthlyBudget: Long,

	@field:PositiveOrZero(message = "usedAmount must be greater than or equal to 0.")
	val usedAmount: Long = 0,
) {
	fun toCreateCommand(): CreateMonthlyBudgetCommand = CreateMonthlyBudgetCommand(
		targetMonth = targetMonth,
		monthlyBudget = monthlyBudget,
		usedAmount = usedAmount,
	)
}
