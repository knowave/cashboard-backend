package com.knowave.cashboard.domains.assetgoal.repository

import com.knowave.cashboard.domains.assetgoal.entity.SavingRecord
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class SavingRecordRepositoryImpl(
	private val savingRecordJpaRepository: SavingRecordJpaRepository,
) : SavingRecordRepository {
	override fun save(savingRecord: SavingRecord): SavingRecord = savingRecordJpaRepository.save(savingRecord)

	override fun findByIdAndUserId(id: UUID, userId: UUID): SavingRecord? =
		savingRecordJpaRepository.findByIdAndUserId(id, userId)

	override fun findByTargetMonthAndUserId(targetMonth: String, userId: UUID): SavingRecord? =
		savingRecordJpaRepository.findByTargetMonthAndUserId(targetMonth, userId)

	override fun findAllByTargetMonthBetweenAndUserIdOrderByTargetMonthDesc(
		fromTargetMonth: String,
		toTargetMonth: String,
		userId: UUID,
	): List<SavingRecord> = savingRecordJpaRepository.findAllByTargetMonthBetweenAndUserIdOrderByTargetMonthDesc(
		fromTargetMonth = fromTargetMonth,
		toTargetMonth = toTargetMonth,
		userId = userId,
	)

	override fun existsByTargetMonthAndUserId(targetMonth: String, userId: UUID): Boolean =
		savingRecordJpaRepository.existsByTargetMonthAndUserId(targetMonth, userId)

	override fun delete(savingRecord: SavingRecord) {
		savingRecordJpaRepository.delete(savingRecord)
	}
}
