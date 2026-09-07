package com.knowave.cashboard.domains.notification.listener

import com.knowave.cashboard.domains.assetgoal.event.AssetGoalChangedEvent
import com.knowave.cashboard.domains.assetgoal.event.TotalAssetAmountChangedEvent
import com.knowave.cashboard.domains.assetgoal.repository.AssetGoalRepository
import com.knowave.cashboard.domains.notification.policy.AssetGoalNotificationPolicy
import com.knowave.cashboard.domains.notification.service.ThresholdNotificationService
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class AssetGoalNotificationEventListener(
	private val assetGoalRepository: AssetGoalRepository,
	private val policy: AssetGoalNotificationPolicy,
	private val thresholdService: ThresholdNotificationService,
) {
	@EventListener
	fun on(event: TotalAssetAmountChangedEvent) {
		assetGoalRepository.findAll().forEach { goal ->
			thresholdService.process(
				policy.evaluate(
					goalId = requireNotNull(goal.id),
					goalName = goal.name,
					previousTargetAmount = goal.targetAmount,
					currentTargetAmount = goal.targetAmount,
					previousAssetAmount = event.previousAmount,
					currentAssetAmount = event.currentAmount,
					occurredAt = event.occurredAt,
				),
				event.occurredAt,
			)
		}
	}

	@EventListener
	fun on(event: AssetGoalChangedEvent) {
		thresholdService.process(
			policy.evaluate(
				goalId = event.goalId,
				goalName = event.goalName,
				previousTargetAmount = event.previousTargetAmount,
				currentTargetAmount = event.currentTargetAmount,
				previousAssetAmount = event.previousAssetAmount,
				currentAssetAmount = event.currentAssetAmount,
				occurredAt = event.occurredAt,
			),
			event.occurredAt,
		)
	}
}
