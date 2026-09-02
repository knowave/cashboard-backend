package com.knowave.cashboard.domains.financialschedule.controller.dto

import com.knowave.cashboard.domains.financialschedule.service.dto.CreateFinancialScheduleCommand
import com.knowave.cashboard.domains.financialschedule.service.dto.RecurrenceCommand
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import java.time.LocalDate

data class FinancialScheduleRecurrenceRequest(
	@field:NotBlank val type: String,
	val scheduledDate: LocalDate? = null,
	val monthOfYear: Int? = null,
	val dayOfMonth: Int? = null,
	val startDate: LocalDate? = null,
	val endDate: LocalDate? = null,
) {
	fun toCommand() = RecurrenceCommand(type, scheduledDate, monthOfYear, dayOfMonth, startDate, endDate)
}

data class FinancialScheduleCreateRequest(
	@field:NotBlank val title: String,
	@field:NotBlank val type: String,
	@field:Positive val amount: Long,
	@field:NotBlank val direction: String,
	@field:Valid val recurrence: FinancialScheduleRecurrenceRequest,
) {
	fun toCommand() = CreateFinancialScheduleCommand(
		type = type,
		title = title,
		amount = amount,
		direction = direction,
		recurrence = recurrence.toCommand(),
	)
}
