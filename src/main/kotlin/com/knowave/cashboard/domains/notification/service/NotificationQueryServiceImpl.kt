package com.knowave.cashboard.domains.notification.service

import com.knowave.cashboard.common.exception.InvalidNotificationPageException
import com.knowave.cashboard.common.exception.NotificationNotFoundException
import com.knowave.cashboard.domains.notification.repository.NotificationRepository
import com.knowave.cashboard.domains.notification.service.dto.NotificationPageResult
import com.knowave.cashboard.domains.notification.service.dto.NotificationResult
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class NotificationQueryServiceImpl(
	private val notificationRepository: NotificationRepository,
) : NotificationQueryService {
	override fun getPage(userId: UUID, page: Int, size: Int, read: Boolean?): NotificationPageResult {
		validatePage(page, size)
		val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id")))
		val notifications = notificationRepository.findPage(userId, read, pageable)
		return NotificationPageResult(
			content = notifications.content.map { it.toResult() },
			page = notifications.number,
			size = notifications.size,
			totalElements = notifications.totalElements,
			totalPages = notifications.totalPages,
			hasNext = notifications.hasNext(),
		)
	}

	override fun get(userId: UUID, id: UUID): NotificationResult =
		notificationRepository.findByIdAndUserId(id, userId)?.toResult() ?: throw NotificationNotFoundException(id)

	override fun countUnread(userId: UUID): Long = notificationRepository.countUnread(userId)

	private fun validatePage(page: Int, size: Int) {
		if (page < 0) throw InvalidNotificationPageException("page must be greater than or equal to 0. page=$page")
		if (size !in 1..100) throw InvalidNotificationPageException("size must be between 1 and 100. size=$size")
	}
}
