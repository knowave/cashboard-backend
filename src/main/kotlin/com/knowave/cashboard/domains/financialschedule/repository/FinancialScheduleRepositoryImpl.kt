package com.knowave.cashboard.domains.financialschedule.repository

import com.knowave.cashboard.domains.financialschedule.entity.FinancialSchedule
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.UUID

@Repository
class FinancialScheduleRepositoryImpl(
	private val financialScheduleJpaRepository: FinancialScheduleJpaRepository,
) : FinancialScheduleRepository {
	override fun save(schedule: FinancialSchedule): FinancialSchedule = financialScheduleJpaRepository.save(schedule)

	override fun findByIdAndUserId(id: UUID, userId: UUID): FinancialSchedule? =
		financialScheduleJpaRepository.findByIdAndUserId(id, userId)

	override fun findAllOrderByCreatedAtDesc(userId: UUID): List<FinancialSchedule> =
		financialScheduleJpaRepository.findAllByUserIdOrderByCreatedAtDescIdAsc(userId)

	override fun findCandidates(userId: UUID, from: LocalDate, toInclusive: LocalDate): List<FinancialSchedule> =
		financialScheduleJpaRepository.findCandidates(userId, from, toInclusive)

	override fun delete(schedule: FinancialSchedule) {
		financialScheduleJpaRepository.delete(schedule)
	}
}
