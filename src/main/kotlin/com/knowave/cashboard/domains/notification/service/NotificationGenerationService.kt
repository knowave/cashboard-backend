package com.knowave.cashboard.domains.notification.service

import com.knowave.cashboard.domains.notification.repository.NewNotification

interface NotificationGenerationService {
	fun createIfEnabled(candidate: NewNotification): Boolean
}
