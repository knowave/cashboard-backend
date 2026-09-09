package com.knowave.cashboard.domains.notification.service

import com.knowave.cashboard.domains.notification.repository.NewNotification
import java.util.UUID

interface NotificationGenerationService {
	fun createIfEnabled(userId: UUID, candidate: NewNotification): Boolean
}
