package com.knowave.cashboard.domains.notification.service

import com.knowave.cashboard.domains.notification.service.dto.NotificationPageResult
import com.knowave.cashboard.domains.notification.service.dto.NotificationResult
import java.util.UUID

interface NotificationQueryService {
	fun getPage(page: Int, size: Int, read: Boolean?): NotificationPageResult
	fun get(id: UUID): NotificationResult
	fun countUnread(): Long
}
