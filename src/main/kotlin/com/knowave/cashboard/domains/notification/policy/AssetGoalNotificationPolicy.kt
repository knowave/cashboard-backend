package com.knowave.cashboard.domains.notification.policy

import com.knowave.cashboard.domains.notification.entity.NotificationType
import com.knowave.cashboard.domains.notification.repository.NewNotification
import org.springframework.stereotype.Component
import java.math.BigInteger
import java.time.Instant
import java.util.UUID

@Component
class AssetGoalNotificationPolicy {
	fun evaluate(
		userId: UUID,
		goalId: UUID,
		goalName: String,
		previousTargetAmount: Long,
		currentTargetAmount: Long,
		previousAssetAmount: Long,
		currentAssetAmount: Long,
		occurredAt: Instant,
	): ThresholdNotificationDecision {
		if (previousTargetAmount <= 0L || currentTargetAmount <= 0L ||
			!isRateIncreasing(previousAssetAmount, previousTargetAmount, currentAssetAmount, currentTargetAmount)
		) {
			return emptyDecision(userId)
		}

		val crossed = MILESTONES.filter { milestone ->
			isBelow(previousAssetAmount, previousTargetAmount, milestone) &&
				isAtLeast(currentAssetAmount, currentTargetAmount, milestone)
		}
		val selected = crossed.lastOrNull() ?: return emptyDecision(userId)
		val selectedKey = policyKey(goalId, selected)
		val type = if (selected == ACHIEVED_MILESTONE) {
			NotificationType.ASSET_GOAL_ACHIEVED
		} else {
			NotificationType.ASSET_GOAL_PROGRESS
		}
		val message = if (selected == ACHIEVED_MILESTONE) {
			"$goalName 목표를 달성했어요."
		} else {
			val remainingAmount = Math.max(0L, Math.subtractExact(currentTargetAmount, currentAssetAmount))
			"$goalName 목표 달성률 $selected%에 도달했어요. 남은 금액은 ${remainingAmount}원이에요."
		}

		return ThresholdNotificationDecision(
			userId = userId,
			crossedPolicyKeys = crossed.map { policyKey(goalId, it) },
			selectedPolicyKey = selectedKey,
			notification = NewNotification(
				userId = userId,
				type = type,
				title = if (selected == ACHIEVED_MILESTONE) "자산 목표 달성" else "자산 목표 진행",
				message = message,
				scheduledAt = occurredAt,
				deduplicationKey = selectedKey,
			),
		)
	}

	private fun emptyDecision(userId: UUID) = ThresholdNotificationDecision(userId, emptyList(), null, null)

	private fun isRateIncreasing(previousAssetAmount: Long, previousTargetAmount: Long, currentAssetAmount: Long, currentTargetAmount: Long): Boolean =
		BigInteger.valueOf(currentAssetAmount).multiply(BigInteger.valueOf(previousTargetAmount)) >
			BigInteger.valueOf(previousAssetAmount).multiply(BigInteger.valueOf(currentTargetAmount))

	private fun isBelow(assetAmount: Long, targetAmount: Long, milestone: Int): Boolean =
		BigInteger.valueOf(assetAmount).multiply(HUNDRED) < BigInteger.valueOf(targetAmount).multiply(BigInteger.valueOf(milestone.toLong()))

	private fun isAtLeast(assetAmount: Long, targetAmount: Long, milestone: Int): Boolean =
		BigInteger.valueOf(assetAmount).multiply(HUNDRED) >= BigInteger.valueOf(targetAmount).multiply(BigInteger.valueOf(milestone.toLong()))

	private fun policyKey(goalId: UUID, milestone: Int): String = "ASSET_GOAL:$goalId:$milestone"

	private companion object {
		val HUNDRED = BigInteger.valueOf(100L)
		val MILESTONES = listOf(50, 80, 100)
		const val ACHIEVED_MILESTONE = 100
	}
}
