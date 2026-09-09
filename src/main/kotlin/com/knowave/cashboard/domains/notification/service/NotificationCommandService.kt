package com.knowave.cashboard.domains.notification.service

import com.knowave.cashboard.domains.notification.service.dto.NotificationResult
import java.util.UUID

interface NotificationCommandService {
	fun markRead(userId: UUID, id: UUID): NotificationResult
	fun markAllRead(userId: UUID): Int
}
