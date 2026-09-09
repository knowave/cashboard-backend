package com.knowave.cashboard.domains.notification.policy

import com.knowave.cashboard.domains.notification.repository.NewNotification
import java.util.UUID

data class ThresholdNotificationDecision(
	val userId: UUID,
	val crossedPolicyKeys: List<String>,
	val selectedPolicyKey: String?,
	val notification: NewNotification?,
)
