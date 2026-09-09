package com.knowave.cashboard.domains.notification.service

import com.knowave.cashboard.domains.notification.service.dto.NotificationSettingCommand
import com.knowave.cashboard.domains.notification.service.dto.NotificationSettingResult
import java.util.UUID

interface NotificationSettingService {
	fun getSettings(userId: UUID): NotificationSettingResult
	fun patchSettings(userId: UUID, command: NotificationSettingCommand): NotificationSettingResult
}
