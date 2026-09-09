package com.knowave.cashboard.domains.assetgoal.service.dto

import com.knowave.cashboard.domains.assetgoal.entity.SavingRecord
import java.util.UUID

data class CreateSavingRecordCommand(
	val targetMonth: String,
	val amount: Long,
	val memo: String?,
) {
	fun toEntity(userId: UUID): SavingRecord = SavingRecord(
		userId = userId,
		targetMonth = targetMonth,
		amount = amount,
		memo = memo,
	)
}

data class UpdateSavingRecordCommand(
	val targetMonth: String,
	val amount: Long,
	val memo: String?,
)
