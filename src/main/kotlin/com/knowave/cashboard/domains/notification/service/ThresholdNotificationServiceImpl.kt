package com.knowave.cashboard.domains.notification.service

import com.knowave.cashboard.domains.notification.policy.ThresholdNotificationDecision
import com.knowave.cashboard.domains.notification.repository.NotificationPolicyMarkerRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class ThresholdNotificationServiceImpl(
	private val markerRepository: NotificationPolicyMarkerRepository,
	private val generationService: NotificationGenerationService,
) : ThresholdNotificationService {
	@Transactional
	override fun process(userId: UUID, decision: ThresholdNotificationDecision, occurredAt: Instant): Boolean {
		val claimed = markerRepository.claimAll(userId, decision.crossedPolicyKeys.toSet(), occurredAt)
		val selectedKey = decision.selectedPolicyKey ?: return false
		val candidate = decision.notification ?: return false
		if (selectedKey !in claimed) return false
		return generationService.createIfEnabled(userId, candidate)
	}
}
