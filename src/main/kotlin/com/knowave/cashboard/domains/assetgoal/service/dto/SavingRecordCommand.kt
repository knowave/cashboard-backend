package com.knowave.cashboard.domains.assetgoal.service.dto

import com.knowave.cashboard.domains.assetgoal.entity.SavingRecord

data class CreateSavingRecordCommand(
	val targetMonth: String,
	val amount: Long,
	val memo: String?,
) {
	fun toEntity(): SavingRecord = SavingRecord(
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
