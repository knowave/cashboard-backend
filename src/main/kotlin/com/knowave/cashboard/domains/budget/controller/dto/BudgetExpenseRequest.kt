package com.knowave.cashboard.domains.budget.controller.dto

import com.knowave.cashboard.domains.budget.service.dto.CreateBudgetExpenseCommand
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.time.LocalDate

data class BudgetExpenseRequest(
	@field:Positive(message = "amount must be greater than 0.")
	val amount: Long,

	@field:Size(max = 50, message = "category must be 50 characters or less.")
	val category: String? = null,

	@field:Size(max = 255, message = "memo must be 255 characters or less.")
	val memo: String? = null,

	@field:NotNull(message = "spentAt is required.")
	val spentAt: LocalDate,
) {
	fun toCommand(): CreateBudgetExpenseCommand = CreateBudgetExpenseCommand(
		amount = amount,
		category = category,
		memo = memo,
		spentAt = spentAt,
	)
}
