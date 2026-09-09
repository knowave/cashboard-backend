package com.knowave.cashboard.domains.notification.service

import com.knowave.cashboard.domains.notification.service.dto.NotificationPageResult
import com.knowave.cashboard.domains.notification.service.dto.NotificationResult
import java.util.UUID

interface NotificationQueryService {
	fun getPage(userId: UUID, page: Int, size: Int, read: Boolean?): NotificationPageResult
	fun get(userId: UUID, id: UUID): NotificationResult
	fun countUnread(userId: UUID): Long
}
