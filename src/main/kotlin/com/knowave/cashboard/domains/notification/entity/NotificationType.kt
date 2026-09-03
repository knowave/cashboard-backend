package com.knowave.cashboard.domains.notification.entity

import com.knowave.cashboard.common.exception.InvalidEnumValueException

enum class NotificationType {
	BUDGET_WARNING,
	BUDGET_EXCEEDED,
	PAYMENT_DUE,
	ASSET_GOAL_PROGRESS,
	ASSET_GOAL_ACHIEVED,
	BALANCE_SHORTAGE,
	WEEKLY_REPORT,
	MONTHLY_REPORT,
	;

	companion object {
		fun from(value: String): NotificationType = entries.firstOrNull {
			it.name == value.trim().uppercase()
		} ?: throw InvalidEnumValueException("NotificationType", value)
	}
}
