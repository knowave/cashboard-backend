package com.knowave.cashboard.domains.assetgoal.repository

import com.knowave.cashboard.domains.assetgoal.entity.SavingRecord
import java.util.UUID

interface SavingRecordRepository {
	fun save(savingRecord: SavingRecord): SavingRecord
	fun findByIdAndUserId(id: UUID, userId: UUID): SavingRecord?
	fun findByTargetMonthAndUserId(targetMonth: String, userId: UUID): SavingRecord?
	fun findAllByTargetMonthBetweenAndUserIdOrderByTargetMonthDesc(
		fromTargetMonth: String,
		toTargetMonth: String,
		userId: UUID,
	): List<SavingRecord>
	fun existsByTargetMonthAndUserId(targetMonth: String, userId: UUID): Boolean
	fun delete(savingRecord: SavingRecord)
}
