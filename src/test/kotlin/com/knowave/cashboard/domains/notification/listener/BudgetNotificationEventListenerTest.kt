package com.knowave.cashboard.domains.notification.listener

import com.knowave.cashboard.domains.budget.event.BudgetUsageChangedEvent
import com.knowave.cashboard.domains.notification.policy.BudgetNotificationPolicy
import com.knowave.cashboard.domains.notification.policy.ThresholdNotificationDecision
import com.knowave.cashboard.domains.notification.service.ThresholdNotificationService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class BudgetNotificationEventListenerTest {
	private val thresholdService = BudgetRecordingThresholdNotificationService()
	private val listener = BudgetNotificationEventListener(BudgetNotificationPolicy(), thresholdService)

	@Test
	fun `리스너는 정책 결정을 같은 호출 흐름에서 처리한다`() {
		val event = event(previousUsed = 70, currentUsed = 105, budget = 100)

		listener.on(event)

		assertThat(thresholdService.decision?.selectedPolicyKey).isEqualTo("BUDGET:${event.monthlyBudgetId}:100")
		assertThat(thresholdService.occurredAt).isEqualTo(event.occurredAt)
	}

	private fun event(previousUsed: Long, currentUsed: Long, budget: Long) = BudgetUsageChangedEvent(
		UUID.randomUUID(), budget, previousUsed, budget, currentUsed, Instant.parse("2026-09-02T00:00:00Z"),
	)
}

private class BudgetRecordingThresholdNotificationService : ThresholdNotificationService {
	var decision: ThresholdNotificationDecision? = null
	var occurredAt: Instant? = null

	override fun process(decision: ThresholdNotificationDecision, occurredAt: Instant): Boolean {
		this.decision = decision
		this.occurredAt = occurredAt
		return true
	}
}
