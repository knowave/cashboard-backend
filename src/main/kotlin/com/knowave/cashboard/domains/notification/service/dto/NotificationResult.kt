package com.knowave.cashboard.domains.notification.service.dto

import com.knowave.cashboard.domains.notification.entity.NotificationStatus
import com.knowave.cashboard.domains.notification.entity.NotificationType
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

data class NotificationResult(
	val id: UUID,
	val type: NotificationType,
	val title: String,
	val message: String,
	val status: NotificationStatus,
	val scheduledAt: Instant,
	val sentAt: Instant?,
	val readAt: Instant?,
	val createdAt: LocalDateTime?,
	val updatedAt: LocalDateTime?,
)

data class NotificationPageResult(
	val content: List<NotificationResult>,
	val page: Int,
	val size: Int,
	val totalElements: Long,
	val totalPages: Int,
	val hasNext: Boolean,
)
