package com.knowave.cashboard.domains.financialschedule.controller.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.knowave.cashboard.domains.financialschedule.service.dto.FinancialScheduleResult
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class FinancialScheduleResponse(
	val id: UUID,
	val type: String,
	val title: String,
	val amount: Long,
	val direction: String,
	val recurrence: FinancialScheduleRecurrenceResponse,
	val createdAt: LocalDateTime?,
	val updatedAt: LocalDateTime?,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class FinancialScheduleRecurrenceResponse(
	val type: String,
	val scheduledDate: LocalDate? = null,
	val monthOfYear: Int? = null,
	val dayOfMonth: Int? = null,
	val startDate: LocalDate? = null,
	val endDate: LocalDate? = null,
)

fun FinancialScheduleResult.toResponse() = FinancialScheduleResponse(
	id = id,
	type = type,
	title = title,
	amount = amount,
	direction = direction,
	recurrence = FinancialScheduleRecurrenceResponse(
		type = recurrence.type,
		scheduledDate = recurrence.scheduledDate,
		monthOfYear = recurrence.monthOfYear,
		dayOfMonth = recurrence.dayOfMonth,
		startDate = recurrence.startDate,
		endDate = recurrence.endDate,
	),
	createdAt = createdAt,
	updatedAt = updatedAt,
)
