package com.knowave.cashboard.domains.financialschedule.entity

import com.knowave.cashboard.common.exception.InvalidEnumValueException

enum class RecurrenceType {
	ONCE,
	MONTHLY,
	YEARLY,
	;

	companion object {
		fun from(value: String): RecurrenceType = entries.firstOrNull {
			it.name == value.trim().uppercase()
		} ?: throw InvalidEnumValueException("RecurrenceType", value)
	}
}
