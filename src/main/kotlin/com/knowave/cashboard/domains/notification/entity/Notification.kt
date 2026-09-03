package com.knowave.cashboard.domains.notification.entity

import com.knowave.cashboard.common.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "notifications")
class Notification private constructor(
	type: String,
	title: String,
	message: String,
	status: String,
	scheduledAt: Instant,
	deduplicationKey: String,
) : BaseEntity() {
	@Column(nullable = false, length = 50)
	final var type: String = type
		private set

	@Column(nullable = false, length = 150)
	final var title: String = title
		private set

	@Column(nullable = false, length = 500)
	final var message: String = message
		private set

	@Column(nullable = false, length = 20)
	final var status: String = status
		private set

	@Column(name = "scheduled_at", nullable = false)
	final var scheduledAt: Instant = scheduledAt
		private set

	@Column(name = "sent_at")
	final var sentAt: Instant? = null
		private set

	@Column(name = "read_at")
	final var readAt: Instant? = null
		private set

	@Column(name = "deduplication_key", nullable = false, unique = true, length = 255)
	final var deduplicationKey: String = deduplicationKey
		private set

	fun markRead(now: Instant) {
		if (readAt == null) {
			readAt = now
		}
	}

	companion object {
		fun create(
			type: NotificationType,
			title: String,
			message: String,
			scheduledAt: Instant,
			deduplicationKey: String,
		): Notification = Notification(
			type = type.name,
			title = title,
			message = message,
			status = NotificationStatus.PENDING.name,
			scheduledAt = scheduledAt,
			deduplicationKey = deduplicationKey,
		)
	}
}
