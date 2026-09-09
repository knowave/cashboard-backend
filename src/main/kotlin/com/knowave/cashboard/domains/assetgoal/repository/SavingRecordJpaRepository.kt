package com.knowave.cashboard.domains.assetgoal.repository

import com.knowave.cashboard.domains.assetgoal.entity.SavingRecord
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SavingRecordJpaRepository : JpaRepository<SavingRecord, UUID> {
	fun findByIdAndUserId(id: UUID, userId: UUID): SavingRecord?
	fun findByTargetMonthAndUserId(targetMonth: String, userId: UUID): SavingRecord?
	fun existsByTargetMonthAndUserId(targetMonth: String, userId: UUID): Boolean
	fun findAllByTargetMonthBetweenAndUserIdOrderByTargetMonthDesc(
		fromTargetMonth: String,
		toTargetMonth: String,
		userId: UUID,
	): List<SavingRecord>
}
