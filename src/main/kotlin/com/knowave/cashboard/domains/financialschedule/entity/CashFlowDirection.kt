package com.knowave.cashboard.domains.financialschedule.entity

import com.knowave.cashboard.common.exception.InvalidEnumValueException

enum class CashFlowDirection {
	INCOME,
	EXPENSE,
	;

	companion object {
		fun from(value: String): CashFlowDirection = entries.firstOrNull {
			it.name == value.trim().uppercase()
		} ?: throw InvalidEnumValueException("CashFlowDirection", value)
	}
}
