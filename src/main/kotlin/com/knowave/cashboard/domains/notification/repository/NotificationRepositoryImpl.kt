package com.knowave.cashboard.domains.notification.repository

import com.knowave.cashboard.domains.notification.entity.Notification
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.sql.Timestamp
import java.util.UUID

@Repository
class NotificationRepositoryImpl(
	private val notificationJpaRepository: NotificationJpaRepository,
	private val jdbcTemplate: NamedParameterJdbcTemplate,
) : NotificationRepository {
	override fun insertIfAbsent(candidate: NewNotification): Boolean = jdbcTemplate.update(
		"""
			INSERT INTO notifications(id, type, title, message, status, scheduled_at, deduplication_key, created_at, updated_at)
			VALUES (:id, :type, :title, :message, 'PENDING', :scheduledAt, :deduplicationKey, LOCALTIMESTAMP, LOCALTIMESTAMP)
			ON CONFLICT (deduplication_key) DO NOTHING
		""".trimIndent(),
		mapOf(
			"id" to candidate.id,
			"type" to candidate.type.name,
			"title" to candidate.title,
			"message" to candidate.message,
			"scheduledAt" to Timestamp.from(candidate.scheduledAt),
			"deduplicationKey" to candidate.deduplicationKey,
		),
	) == 1

	override fun findById(id: UUID): Notification? = notificationJpaRepository.findById(id).orElse(null)

	override fun findPage(read: Boolean?, pageable: Pageable): Page<Notification> {
		val orderedPageable = PageRequestWithNotificationOrder.of(pageable)
		return when (read) {
			null -> notificationJpaRepository.findAll(orderedPageable)
			true -> notificationJpaRepository.findAllByReadAtIsNotNull(orderedPageable)
			false -> notificationJpaRepository.findAllByReadAtIsNull(orderedPageable)
		}
	}

	override fun countUnread(): Long = notificationJpaRepository.countByReadAtIsNull()

	override fun save(notification: Notification): Notification = notificationJpaRepository.save(notification)

	@Transactional
	override fun markReadIfUnread(id: UUID, now: Instant): ConditionalReadResult? {
		val changed = notificationJpaRepository.markReadIfUnread(id, now) == 1
		val notification = notificationJpaRepository.findById(id).orElse(null) ?: return null
		return ConditionalReadResult(notification, changed)
	}

	override fun markAllRead(now: Instant): Int = notificationJpaRepository.markAllRead(now)
}

private object PageRequestWithNotificationOrder {
	private val sort = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))

	fun of(pageable: Pageable): Pageable = org.springframework.data.domain.PageRequest.of(
		pageable.pageNumber,
		pageable.pageSize,
		sort,
	)
}
