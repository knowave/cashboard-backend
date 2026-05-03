package com.knowave.cashboard.domains.fixedexpense.controller.dto

import com.knowave.cashboard.domains.fixedexpense.service.dto.CreateFixedExpenseCommand
import com.knowave.cashboard.domains.fixedexpense.service.dto.UpdateFixedExpenseCommand
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import java.time.YearMonth

data class FixedExpenseRequest(
	@field:NotBlank(message = "name is required.")
	val name: String,

	@field:Min(value = 0, message = "amount must be greater than or equal to 0.")
	val amount: Long,

	@field:NotBlank(message = "category is required.")
	val category: String,

	@field:Pattern(regexp = "\\d{4}-\\d{2}", message = "startMonth must be yyyy-MM.")
	val startMonth: String,

	@field:Pattern(regexp = "\\d{4}-\\d{2}", message = "endMonth must be yyyy-MM.")
	val endMonth: String? = null,
) {
	fun toCreateCommand(): CreateFixedExpenseCommand = CreateFixedExpenseCommand(
		name = name,
		amount = amount,
		category = category,
		startMonth = YearMonth.parse(startMonth),
		endMonth = endMonth?.let { YearMonth.parse(it) },
	)

	fun toUpdateCommand(): UpdateFixedExpenseCommand = UpdateFixedExpenseCommand(
		name = name,
		amount = amount,
		category = category,
		startMonth = YearMonth.parse(startMonth),
		endMonth = endMonth?.let { YearMonth.parse(it) },
	)
}
