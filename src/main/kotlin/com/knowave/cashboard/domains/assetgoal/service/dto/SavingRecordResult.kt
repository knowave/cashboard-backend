package com.knowave.cashboard.domains.assetgoal.service.dto

import com.knowave.cashboard.domains.assetgoal.entity.SavingRecord
import java.time.LocalDateTime
import java.util.UUID

data class SavingRecordResult(
	val id: UUID,
	val targetMonth: String,
	val amount: Long,
	val memo: String?,
	val createdAt: LocalDateTime,
	val updatedAt: LocalDateTime,
)

fun SavingRecord.toResult(): SavingRecordResult = SavingRecordResult(
	id = requireNotNull(id),
	targetMonth = targetMonth,
	amount = amount,
	memo = memo,
	createdAt = requireNotNull(createdAt),
	updatedAt = requireNotNull(updatedAt),
)
