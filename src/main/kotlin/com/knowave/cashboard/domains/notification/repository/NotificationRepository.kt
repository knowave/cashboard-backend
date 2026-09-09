package com.knowave.cashboard.domains.notification.repository

import com.knowave.cashboard.domains.notification.entity.Notification
import com.knowave.cashboard.domains.notification.entity.NotificationType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.Instant
import java.util.UUID

data class NewNotification(
	val id: UUID = UUID.randomUUID(),
	val userId: UUID,
	val type: NotificationType,
	val title: String,
	val message: String,
	val scheduledAt: Instant,
	val deduplicationKey: String,
)

data class ConditionalReadResult(
	val notification: Notification,
	val changed: Boolean,
)

interface NotificationRepository {
	fun insertIfAbsent(candidate: NewNotification): Boolean
	fun findByIdAndUserId(id: UUID, userId: UUID): Notification?
	fun findPage(userId: UUID, read: Boolean?, pageable: Pageable): Page<Notification>
	fun countUnread(userId: UUID): Long
	fun save(notification: Notification): Notification
	fun markReadIfUnread(id: UUID, userId: UUID, now: Instant): ConditionalReadResult?
	fun markAllRead(userId: UUID, now: Instant): Int
}
