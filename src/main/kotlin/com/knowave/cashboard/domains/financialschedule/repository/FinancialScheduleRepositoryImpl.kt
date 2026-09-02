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

	override fun findById(id: UUID): FinancialSchedule? = financialScheduleJpaRepository.findById(id).orElse(null)

	override fun findAllOrderByCreatedAtDesc(): List<FinancialSchedule> =
		financialScheduleJpaRepository.findAllByOrderByCreatedAtDescIdAsc()

	override fun findCandidates(from: LocalDate, toInclusive: LocalDate): List<FinancialSchedule> =
		financialScheduleJpaRepository.findCandidates(from, toInclusive)

	override fun delete(schedule: FinancialSchedule) {
		financialScheduleJpaRepository.delete(schedule)
	}
}
