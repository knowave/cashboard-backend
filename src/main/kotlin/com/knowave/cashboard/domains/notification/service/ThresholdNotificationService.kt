package com.knowave.cashboard.domains.notification.service

import com.knowave.cashboard.domains.notification.policy.ThresholdNotificationDecision
import java.time.Instant
import java.util.UUID

interface ThresholdNotificationService {
	fun process(userId: UUID, decision: ThresholdNotificationDecision, occurredAt: Instant): Boolean
}
