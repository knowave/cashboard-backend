package com.knowave.cashboard.domains.assetgoal.repository

import com.knowave.cashboard.domains.assetgoal.entity.SavingRecord
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class SavingRecordRepositoryImpl(
	private val savingRecordJpaRepository: SavingRecordJpaRepository,
) : SavingRecordRepository {
	override fun save(savingRecord: SavingRecord): SavingRecord = savingRecordJpaRepository.save(savingRecord)

	override fun findById(id: UUID): SavingRecord? = savingRecordJpaRepository.findById(id).orElse(null)

	override fun findByTargetMonth(targetMonth: String): SavingRecord? =
		savingRecordJpaRepository.findByTargetMonth(targetMonth)

	override fun findAllByTargetMonthBetweenOrderByTargetMonthDesc(
		fromTargetMonth: String,
		toTargetMonth: String,
	): List<SavingRecord> = savingRecordJpaRepository.findAllByTargetMonthBetweenOrderByTargetMonthDesc(
		fromTargetMonth = fromTargetMonth,
		toTargetMonth = toTargetMonth,
	)

	override fun existsByTargetMonth(targetMonth: String): Boolean =
		savingRecordJpaRepository.existsByTargetMonth(targetMonth)

	override fun delete(savingRecord: SavingRecord) {
		savingRecordJpaRepository.delete(savingRecord)
	}
}
