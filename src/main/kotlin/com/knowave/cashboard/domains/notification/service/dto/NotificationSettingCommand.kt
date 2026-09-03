package com.knowave.cashboard.domains.notification.service.dto

import com.knowave.cashboard.domains.notification.entity.NotificationType

data class NotificationSettingCommand(
	val pushEnabled: Boolean? = null,
	val settings: Map<NotificationType, Boolean>? = null,
)

data class NotificationSettingResult(
	val pushEnabled: Boolean,
	val settings: Map<String, Boolean>,
)
