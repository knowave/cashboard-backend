package com.knowave.cashboard.domains.notification.controller.dto

import com.knowave.cashboard.domains.notification.entity.NotificationType
import com.knowave.cashboard.domains.notification.service.dto.NotificationSettingCommand

data class NotificationSettingPatchRequest(
    val pushEnabled: Boolean? = null,
    val settings: Map<String, Boolean>? = null,
) {
    fun toCommand() = NotificationSettingCommand(
        pushEnabled = pushEnabled,
        settings = settings?.mapKeys { NotificationType.from(it.key) },
    )
}
