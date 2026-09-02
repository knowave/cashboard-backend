package com.knowave.cashboard.domains.financialschedule.calculator

import com.knowave.cashboard.domains.financialschedule.entity.CashFlowDirection
import com.knowave.cashboard.domains.financialschedule.entity.RecurrenceRule
import com.knowave.cashboard.domains.financialschedule.entity.ScheduleType
import java.time.LocalDate
import java.util.UUID

data class ScheduleDefinition(
	val scheduleId: UUID,
	val type: ScheduleType,
	val title: String,
	val amount: Long,
	val direction: CashFlowDirection,
	val recurrence: RecurrenceRule,
)

data class ScheduleOccurrence(
	val scheduleId: UUID,
	val date: LocalDate,
	val type: ScheduleType,
	val title: String,
	val amount: Long,
	val direction: CashFlowDirection,
)
