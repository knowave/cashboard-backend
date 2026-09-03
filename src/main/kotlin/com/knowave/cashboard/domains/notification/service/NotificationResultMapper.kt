package com.knowave.cashboard.domains.notification.service

import com.knowave.cashboard.common.exception.InvalidEnumValueException
import com.knowave.cashboard.common.exception.NotificationDataIntegrityException
import com.knowave.cashboard.domains.notification.entity.Notification
import com.knowave.cashboard.domains.notification.entity.NotificationStatus
import com.knowave.cashboard.domains.notification.entity.NotificationType
import com.knowave.cashboard.domains.notification.service.dto.NotificationResult
import org.slf4j.LoggerFactory
import java.util.UUID

fun Notification.toResult(): NotificationResult {
	val notificationId = requireNotNull(id) { "Persisted notification id must not be null" }
	return NotificationResult(
		id = notificationId,
		type = type.toNotificationType(notificationId),
		title = title,
		message = message,
		status = status.toNotificationStatus(notificationId),
		scheduledAt = scheduledAt,
		sentAt = sentAt,
		readAt = readAt,
		createdAt = createdAt,
		updatedAt = updatedAt,
	)
}

private fun String.toNotificationType(notificationId: UUID): NotificationType = try {
	NotificationType.from(this)
} catch (cause: InvalidEnumValueException) {
	throw dataIntegrityException(notificationId, "type", cause)
}

private fun String.toNotificationStatus(notificationId: UUID): NotificationStatus = try {
	NotificationStatus.from(this)
} catch (cause: InvalidEnumValueException) {
	throw dataIntegrityException(notificationId, "status", cause)
}

private fun dataIntegrityException(
	notificationId: UUID,
	field: String,
	cause: InvalidEnumValueException,
): NotificationDataIntegrityException {
	logger.error("Invalid persisted notification value. id={}, field={}", notificationId, field, cause)
	return NotificationDataIntegrityException(notificationId, field, cause)
}

private val logger = LoggerFactory.getLogger("NotificationResultMapper")
