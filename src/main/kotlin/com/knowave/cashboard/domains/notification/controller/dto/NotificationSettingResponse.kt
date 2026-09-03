package com.knowave.cashboard.domains.notification.controller.dto

import com.knowave.cashboard.domains.notification.service.dto.NotificationSettingResult

data class NotificationSettingResponse(
    val pushEnabled: Boolean,
    val settings: Map<String, Boolean>,
)

fun NotificationSettingResult.toResponse() = NotificationSettingResponse(
    pushEnabled = pushEnabled,
    settings = settings,
)
