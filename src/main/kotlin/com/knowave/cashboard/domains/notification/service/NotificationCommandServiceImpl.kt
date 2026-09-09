package com.knowave.cashboard.domains.notification.service

import com.knowave.cashboard.common.exception.NotificationNotFoundException
import com.knowave.cashboard.domains.notification.repository.NotificationRepository
import com.knowave.cashboard.domains.notification.service.dto.NotificationResult
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant
import java.util.UUID

@Service
@Transactional
class NotificationCommandServiceImpl(
	private val notificationRepository: NotificationRepository,
	private val clock: Clock,
) : NotificationCommandService {
	override fun markRead(userId: UUID, id: UUID): NotificationResult {
		val result = notificationRepository.markReadIfUnread(id, userId, clock.instant())
			?: throw NotificationNotFoundException(id)
		return result.notification.toResult()
	}

	override fun markAllRead(userId: UUID): Int = notificationRepository.markAllRead(userId, Instant.now(clock))
}
