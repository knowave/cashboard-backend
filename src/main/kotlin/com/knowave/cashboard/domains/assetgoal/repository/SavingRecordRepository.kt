package com.knowave.cashboard.domains.assetgoal.repository

import com.knowave.cashboard.domains.assetgoal.entity.SavingRecord
import java.util.UUID

interface SavingRecordRepository {
	fun save(savingRecord: SavingRecord): SavingRecord
	fun findById(id: UUID): SavingRecord?
	fun findByTargetMonth(targetMonth: String): SavingRecord?
	fun findAllByTargetMonthBetweenOrderByTargetMonthDesc(
		fromTargetMonth: String,
		toTargetMonth: String,
	): List<SavingRecord>
	fun existsByTargetMonth(targetMonth: String): Boolean
	fun delete(savingRecord: SavingRecord)
}
