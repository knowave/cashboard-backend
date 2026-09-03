package com.knowave.cashboard.domains.notification.service

import com.knowave.cashboard.domains.notification.service.dto.NotificationSettingCommand
import com.knowave.cashboard.domains.notification.service.dto.NotificationSettingResult

interface NotificationSettingService {
	fun getSettings(): NotificationSettingResult
	fun patchSettings(command: NotificationSettingCommand): NotificationSettingResult
}
