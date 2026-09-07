package com.knowave.cashboard.domains.notification.service

import com.knowave.cashboard.domains.notification.policy.ThresholdNotificationDecision
import java.time.Instant

interface ThresholdNotificationService {
	fun process(decision: ThresholdNotificationDecision, occurredAt: Instant): Boolean
}
