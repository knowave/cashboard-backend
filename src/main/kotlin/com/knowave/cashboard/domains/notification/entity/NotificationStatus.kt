package com.knowave.cashboard.domains.notification.entity

import com.knowave.cashboard.common.exception.InvalidEnumValueException

enum class NotificationStatus {
	PENDING,
	SENT,
	FAILED,
	;

	companion object {
		fun from(value: String): NotificationStatus = entries.firstOrNull {
			it.name == value.trim().uppercase()
		} ?: throw InvalidEnumValueException("NotificationStatus", value)
	}
}
