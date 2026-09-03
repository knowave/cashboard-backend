package com.knowave.cashboard.domains.notification.sender

import java.util.UUID

interface NotificationSender {
	fun send(notificationId: UUID)
}
