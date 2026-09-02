package com.knowave.cashboard.domains.financialschedule.service.dto

import com.knowave.cashboard.domains.financialschedule.entity.CashFlowDirection
import com.knowave.cashboard.domains.financialschedule.entity.FinancialSchedule
import com.knowave.cashboard.domains.financialschedule.entity.RecurrenceType
import com.knowave.cashboard.domains.financialschedule.entity.ScheduleType
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class FinancialScheduleResult(
	val id: UUID,
	val type: String,
	val title: String,
	val amount: Long,
	val direction: String,
	val recurrence: RecurrenceResult,
	val createdAt: LocalDateTime?,
	val updatedAt: LocalDateTime?,
)

data class RecurrenceResult(
	val type: String,
	val scheduledDate: LocalDate? = null,
	val monthOfYear: Int? = null,
	val dayOfMonth: Int? = null,
	val startDate: LocalDate? = null,
	val endDate: LocalDate? = null,
)

fun FinancialSchedule.toResult(): FinancialScheduleResult {
	val scheduleId = requireNotNull(id)
	val type = ScheduleType.from(scheduleType)
	val direction = CashFlowDirection.from(direction)
	val recurrenceType = RecurrenceType.from(recurrenceType)

	return FinancialScheduleResult(
		id = scheduleId,
		type = type.name,
		title = title,
		amount = amount,
		direction = direction.name,
		recurrence = when (recurrenceType) {
			RecurrenceType.ONCE -> RecurrenceResult(type = recurrenceType.name, scheduledDate = scheduledDate)
			RecurrenceType.MONTHLY -> RecurrenceResult(
				type = recurrenceType.name,
				dayOfMonth = dayOfMonth,
				startDate = startDate,
				endDate = endDate,
			)
			RecurrenceType.YEARLY -> RecurrenceResult(
				type = recurrenceType.name,
				monthOfYear = monthOfYear,
				dayOfMonth = dayOfMonth,
				startDate = startDate,
				endDate = endDate,
			)
		},
		createdAt = createdAt,
		updatedAt = updatedAt,
	)
}
