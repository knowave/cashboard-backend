package com.knowave.cashboard.domains.account.entity

import com.knowave.cashboard.common.exception.InvalidEnumValueException

enum class AccountType {
	LIQUID,
	EMERGENCY,
	LACKED,
	INVESTMENT,
	;

	companion object {
		fun from(value: String): AccountType = entries.firstOrNull { it.name == value.uppercase() }
			?: throw InvalidEnumValueException("AccountType", value)
	}
}
