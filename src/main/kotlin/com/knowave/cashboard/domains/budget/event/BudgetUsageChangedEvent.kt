package com.knowave.cashboard.domains.budget.event

import java.time.Instant
import java.util.UUID

data class BudgetUsageChangedEvent(
	val userId: UUID,
	val monthlyBudgetId: UUID,
	val previousBudgetAmount: Long,
	val previousUsedAmount: Long,
	val currentBudgetAmount: Long,
	val currentUsedAmount: Long,
	val occurredAt: Instant,
)
