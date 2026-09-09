package com.knowave.cashboard.domains.notification.service

import com.knowave.cashboard.domains.notification.repository.NewNotification
import com.knowave.cashboard.domains.notification.repository.NotificationRepository
import com.knowave.cashboard.domains.notification.repository.NotificationSettingRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class NotificationGenerationServiceImpl(
	private val notificationRepository: NotificationRepository,
	private val settingRepository: NotificationSettingRepository,
) : NotificationGenerationService {
	override fun createIfEnabled(userId: UUID, candidate: NewNotification): Boolean {
		if (!settingRepository.isEnabled(userId, candidate.type)) return false
		return notificationRepository.insertIfAbsent(candidate)
	}
}
