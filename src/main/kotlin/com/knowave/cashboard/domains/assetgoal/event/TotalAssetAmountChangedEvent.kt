package com.knowave.cashboard.domains.assetgoal.event

import java.time.Instant

data class TotalAssetAmountChangedEvent(
	val previousAmount: Long,
	val currentAmount: Long,
	val occurredAt: Instant,
)
