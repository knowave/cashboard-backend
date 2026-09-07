package com.knowave.cashboard.domains.assetgoal.event

import java.time.Instant
import java.util.UUID

data class AssetGoalChangedEvent(
	val goalId: UUID,
	val goalName: String,
	val previousTargetAmount: Long,
	val currentTargetAmount: Long,
	val previousAssetAmount: Long,
	val currentAssetAmount: Long,
	val occurredAt: Instant,
)
