package com.knowave.cashboard.domains.notification.policy

import com.knowave.cashboard.domains.notification.repository.NewNotification

data class ThresholdNotificationDecision(
	val crossedPolicyKeys: List<String>,
	val selectedPolicyKey: String?,
	val notification: NewNotification?,
)
