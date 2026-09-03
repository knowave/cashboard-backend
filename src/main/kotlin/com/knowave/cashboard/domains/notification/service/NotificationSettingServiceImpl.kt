package com.knowave.cashboard.domains.notification.service

import com.knowave.cashboard.domains.notification.entity.NotificationType
import com.knowave.cashboard.domains.notification.repository.NotificationPreferenceRepository
import com.knowave.cashboard.domains.notification.repository.NotificationSettingRepository
import com.knowave.cashboard.domains.notification.service.dto.NotificationSettingCommand
import com.knowave.cashboard.domains.notification.service.dto.NotificationSettingResult
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class NotificationSettingServiceImpl(
	private val settingRepository: NotificationSettingRepository,
	private val preferenceRepository: NotificationPreferenceRepository,
) : NotificationSettingService {
	override fun getSettings(): NotificationSettingResult = settingsResult()

	@Transactional
	override fun patchSettings(command: NotificationSettingCommand): NotificationSettingResult {
		command.pushEnabled?.let { preferenceRepository.upsertPushEnabled(it) }
		command.settings?.forEach { (type, enabled) -> settingRepository.upsert(type, enabled) }
		return settingsResult()
	}

	private fun settingsResult(): NotificationSettingResult = NotificationSettingResult(
		pushEnabled = preferenceRepository.getPushEnabled(),
		settings = NotificationType.entries.associate { type -> type.name to settingRepository.isEnabled(type) },
	)
}
