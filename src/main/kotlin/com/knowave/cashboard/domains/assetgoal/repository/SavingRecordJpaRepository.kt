package com.knowave.cashboard.domains.assetgoal.repository

import com.knowave.cashboard.domains.assetgoal.entity.SavingRecord
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SavingRecordJpaRepository : JpaRepository<SavingRecord, UUID> {
	fun findByTargetMonth(targetMonth: String): SavingRecord?
	fun existsByTargetMonth(targetMonth: String): Boolean
	fun findAllByTargetMonthBetweenOrderByTargetMonthDesc(
		fromTargetMonth: String,
		toTargetMonth: String,
	): List<SavingRecord>
}
