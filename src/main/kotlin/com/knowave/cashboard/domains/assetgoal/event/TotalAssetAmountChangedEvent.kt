package com.knowave.cashboard.domains.assetgoal.event

import java.time.Instant
import java.util.UUID

data class TotalAssetAmountChangedEvent(
	val userId: UUID,
	val previousAmount: Long,
	val currentAmount: Long,
	val occurredAt: Instant,
)
