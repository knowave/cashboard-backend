package com.knowave.cashboard.common.exception

import org.springframework.http.HttpStatus
import java.util.UUID

class NotificationNotFoundException(id: UUID) : CashboardException(
	errorCode = "NOTIFICATION_NOT_FOUND",
	message = "Notification not found. id=$id",
	status = HttpStatus.NOT_FOUND,
)

class InvalidNotificationPageException(reason: String) : CashboardException(
	errorCode = "INVALID_NOTIFICATION_PAGE",
	message = reason,
)

class NotificationDataIntegrityException(id: UUID, field: String, cause: Throwable) : CashboardException(
	errorCode = "DATA_INTEGRITY_ERROR",
	message = "Notification contains an invalid persisted value. id=$id, field=$field",
	status = HttpStatus.INTERNAL_SERVER_ERROR,
) {
	init {
		initCause(cause)
	}
}
