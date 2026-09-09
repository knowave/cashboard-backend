package com.knowave.cashboard.domains.notification.policy

import com.knowave.cashboard.domains.budget.event.BudgetUsageChangedEvent
import com.knowave.cashboard.domains.notification.entity.NotificationType
import com.knowave.cashboard.domains.notification.repository.NewNotification
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
class BudgetNotificationPolicy {
	fun evaluate(event: BudgetUsageChangedEvent): ThresholdNotificationDecision {
		if (event.previousBudgetAmount <= 0 || event.currentBudgetAmount <= 0) {
			return ThresholdNotificationDecision(event.userId, emptyList(), null, null)
		}

		val crossedThresholds = THRESHOLDS.filter { threshold ->
			crossed(
				previousUsed = event.previousUsedAmount,
				previousBudget = event.previousBudgetAmount,
				currentUsed = event.currentUsedAmount,
				currentBudget = event.currentBudgetAmount,
				threshold = threshold,
			)
		}
		val selectedThreshold = crossedThresholds.maxOrNull()
		val crossedKeys = crossedThresholds.map { threshold -> policyKey(event, threshold) }
		val selectedKey = selectedThreshold?.let { threshold -> policyKey(event, threshold) }

		return ThresholdNotificationDecision(
			userId = event.userId,
			crossedPolicyKeys = crossedKeys,
			selectedPolicyKey = selectedKey,
			notification = selectedThreshold?.let { threshold -> notification(event, threshold, requireNotNull(selectedKey)) },
		)
	}

	private fun crossed(
		previousUsed: Long,
		previousBudget: Long,
		currentUsed: Long,
		currentBudget: Long,
		threshold: Int,
	): Boolean = BigInteger.valueOf(previousUsed).multiply(HUNDRED) <
		BigInteger.valueOf(previousBudget).multiply(BigInteger.valueOf(threshold.toLong())) &&
		BigInteger.valueOf(currentUsed).multiply(HUNDRED) >=
			BigInteger.valueOf(currentBudget).multiply(BigInteger.valueOf(threshold.toLong()))

	private fun policyKey(event: BudgetUsageChangedEvent, threshold: Int): String =
		"BUDGET:${event.monthlyBudgetId}:$threshold"

	private fun notification(event: BudgetUsageChangedEvent, threshold: Int, key: String): NewNotification {
		val type = if (threshold == WARNING_THRESHOLD) NotificationType.BUDGET_WARNING else NotificationType.BUDGET_EXCEEDED
		val title = if (threshold == WARNING_THRESHOLD) "이번 달 예산이 얼마 남지 않았어요" else "이번 달 예산을 초과했어요"
		val usageRate = BigInteger.valueOf(event.currentUsedAmount)
			.multiply(HUNDRED)
			.divide(BigInteger.valueOf(event.currentBudgetAmount))
		return NewNotification(
			userId = event.userId,
			type = type,
			title = title,
			message = "현재 예산 사용률은 $usageRate%예요.",
			scheduledAt = event.occurredAt,
			deduplicationKey = key,
		)
	}

	private companion object {
		val HUNDRED: BigInteger = BigInteger.valueOf(100)
		const val WARNING_THRESHOLD = 80
		val THRESHOLDS = listOf(WARNING_THRESHOLD, 100)
	}
}
