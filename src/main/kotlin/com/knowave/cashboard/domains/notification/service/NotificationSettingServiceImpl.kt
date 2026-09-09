package com.knowave.cashboard.domains.notification.service

import com.knowave.cashboard.domains.notification.entity.NotificationType
import com.knowave.cashboard.domains.notification.repository.NotificationPreferenceRepository
import com.knowave.cashboard.domains.notification.repository.NotificationSettingRepository
import com.knowave.cashboard.domains.notification.service.dto.NotificationSettingCommand
import com.knowave.cashboard.domains.notification.service.dto.NotificationSettingResult
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class NotificationSettingServiceImpl(
	private val settingRepository: NotificationSettingRepository,
	private val preferenceRepository: NotificationPreferenceRepository,
) : NotificationSettingService {
	override fun getSettings(userId: UUID): NotificationSettingResult = settingsResult(userId)

	@Transactional
	override fun patchSettings(userId: UUID, command: NotificationSettingCommand): NotificationSettingResult {
		command.pushEnabled?.let { preferenceRepository.upsertPushEnabled(userId, it) }
		command.settings?.forEach { (type, enabled) -> settingRepository.upsert(userId, type, enabled) }
		return settingsResult(userId)
	}

	private fun settingsResult(userId: UUID): NotificationSettingResult = NotificationSettingResult(
		pushEnabled = preferenceRepository.getPushEnabled(userId),
		settings = NotificationType.entries.associate { type -> type.name to settingRepository.isEnabled(userId, type) },
	)
}
