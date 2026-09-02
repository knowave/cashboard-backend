package com.knowave.cashboard.domains.financialschedule.controller.dto

import com.fasterxml.jackson.annotation.JsonSetter
import com.knowave.cashboard.domains.financialschedule.service.dto.PatchField
import com.knowave.cashboard.domains.financialschedule.service.dto.PatchFinancialScheduleCommand

class FinancialSchedulePatchRequest {
	private var type: PatchField<String> = PatchField.Absent
	private var title: PatchField<String> = PatchField.Absent
	private var amount: PatchField<Long> = PatchField.Absent
	private var direction: PatchField<String> = PatchField.Absent
	private var recurrence: PatchField<FinancialScheduleRecurrenceRequest> = PatchField.Absent

	@JsonSetter("type")
	fun assignType(value: String?) {
		type = PatchField.Present(value)
	}

	@JsonSetter("title")
	fun assignTitle(value: String?) {
		title = PatchField.Present(value)
	}

	@JsonSetter("amount")
	fun assignAmount(value: Long?) {
		amount = PatchField.Present(value)
	}

	@JsonSetter("direction")
	fun assignDirection(value: String?) {
		direction = PatchField.Present(value)
	}

	@JsonSetter("recurrence")
	fun assignRecurrence(value: FinancialScheduleRecurrenceRequest?) {
		recurrence = PatchField.Present(value)
	}

	fun toCommand() = PatchFinancialScheduleCommand(
		type = type,
		title = title,
		amount = amount,
		direction = direction,
		recurrence = recurrence.map { it.toCommand() },
	)
}

private inline fun <T, R> PatchField<T>.map(transform: (T) -> R): PatchField<R> = when (this) {
	PatchField.Absent -> PatchField.Absent
	is PatchField.Present -> PatchField.Present(value?.let(transform))
}
