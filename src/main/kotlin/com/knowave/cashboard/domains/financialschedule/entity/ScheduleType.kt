package com.knowave.cashboard.domains.financialschedule.entity

import com.knowave.cashboard.common.exception.InvalidEnumValueException

enum class ScheduleType {
	SALARY,
	RENT,
	CARD,
	LOAN,
	SAVING,
	SUBSCRIPTION,
	INSURANCE,
	UTILITY,
	ETC,
	;

	companion object {
		fun from(value: String): ScheduleType = entries.firstOrNull {
			it.name == value.trim().uppercase()
		} ?: throw InvalidEnumValueException("ScheduleType", value)
	}
}
