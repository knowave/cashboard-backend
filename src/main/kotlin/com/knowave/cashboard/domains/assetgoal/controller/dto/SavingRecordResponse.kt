package com.knowave.cashboard.domains.assetgoal.controller.dto

import com.knowave.cashboard.domains.assetgoal.service.dto.SavingRecordResult
import java.time.LocalDateTime
import java.util.UUID

data class SavingRecordResponse(
	val id: UUID,
	val targetMonth: String,
	val amount: Long,
	val memo: String?,
	val createdAt: LocalDateTime,
	val updatedAt: LocalDateTime,
)

fun SavingRecordResult.toResponse(): SavingRecordResponse = SavingRecordResponse(
	id = id,
	targetMonth = targetMonth,
	amount = amount,
	memo = memo,
	createdAt = createdAt,
	updatedAt = updatedAt,
)
