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
	override fun markRead(id: UUID): NotificationResult {
		val result = notificationRepository.markReadIfUnread(id, clock.instant())
			?: throw NotificationNotFoundException(id)
		return result.notification.toResult()
	}

	override fun markAllRead(): Int = notificationRepository.markAllRead(Instant.now(clock))
}
