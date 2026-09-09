package com.knowave.cashboard.domains.notification.listener

import com.knowave.cashboard.domains.budget.event.BudgetUsageChangedEvent
import com.knowave.cashboard.domains.notification.policy.BudgetNotificationPolicy
import com.knowave.cashboard.domains.notification.service.ThresholdNotificationService
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class BudgetNotificationEventListener(
	private val policy: BudgetNotificationPolicy,
	private val thresholdService: ThresholdNotificationService,
) {
	@EventListener
	fun on(event: BudgetUsageChangedEvent) {
		thresholdService.process(event.userId, policy.evaluate(event), event.occurredAt)
	}
}
