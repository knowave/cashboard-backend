package com.knowave.cashboard.domains.financialschedule.service.dto

import com.knowave.cashboard.common.exception.InvalidRecurrenceRuleException
import com.knowave.cashboard.domains.financialschedule.entity.RecurrenceRule
import com.knowave.cashboard.domains.financialschedule.entity.RecurrenceType
import java.time.LocalDate

data class RecurrenceCommand(
	val type: String,
	val scheduledDate: LocalDate? = null,
	val monthOfYear: Int? = null,
	val dayOfMonth: Int? = null,
	val startDate: LocalDate? = null,
	val endDate: LocalDate? = null,
)

data class CreateFinancialScheduleCommand(
	val type: String,
	val title: String,
	val amount: Long,
	val direction: String,
	val recurrence: RecurrenceCommand,
)

sealed interface PatchField<out T> {
	data object Absent : PatchField<Nothing>
	data class Present<T>(val value: T?) : PatchField<T>
}

data class PatchFinancialScheduleCommand(
	val type: PatchField<String> = PatchField.Absent,
	val title: PatchField<String> = PatchField.Absent,
	val amount: PatchField<Long> = PatchField.Absent,
	val direction: PatchField<String> = PatchField.Absent,
	val recurrence: PatchField<RecurrenceCommand> = PatchField.Absent,
)

fun RecurrenceCommand.toRecurrenceRule(): RecurrenceRule = when (RecurrenceType.from(type)) {
	RecurrenceType.ONCE -> {
		if (monthOfYear != null || dayOfMonth != null || startDate != null || endDate != null) {
			throw InvalidRecurrenceRuleException("ONCE accepts only scheduledDate.")
		}
		RecurrenceRule.Once(
			scheduledDate ?: throw InvalidRecurrenceRuleException("scheduledDate is required for ONCE."),
		)
	}
	RecurrenceType.MONTHLY -> {
		if (scheduledDate != null || monthOfYear != null) {
			throw InvalidRecurrenceRuleException("MONTHLY does not accept scheduledDate or monthOfYear.")
		}
		RecurrenceRule.Monthly(
			dayOfMonth = dayOfMonth ?: throw InvalidRecurrenceRuleException("dayOfMonth is required for MONTHLY."),
			startDate = startDate ?: throw InvalidRecurrenceRuleException("startDate is required for MONTHLY."),
			endDate = endDate,
		)
	}
	RecurrenceType.YEARLY -> {
		if (scheduledDate != null) {
			throw InvalidRecurrenceRuleException("YEARLY does not accept scheduledDate.")
		}
		RecurrenceRule.Yearly(
			monthOfYear = monthOfYear ?: throw InvalidRecurrenceRuleException("monthOfYear is required for YEARLY."),
			dayOfMonth = dayOfMonth ?: throw InvalidRecurrenceRuleException("dayOfMonth is required for YEARLY."),
			startDate = startDate ?: throw InvalidRecurrenceRuleException("startDate is required for YEARLY."),
			endDate = endDate,
		)
	}
}
