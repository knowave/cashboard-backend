package com.knowave.cashboard.domains.financialschedule.repository

import com.knowave.cashboard.domains.financialschedule.entity.FinancialSchedule
import java.time.LocalDate
import java.util.UUID

interface FinancialScheduleRepository {
	fun save(schedule: FinancialSchedule): FinancialSchedule
	fun findByIdAndUserId(id: UUID, userId: UUID): FinancialSchedule?
	fun findAllOrderByCreatedAtDesc(userId: UUID): List<FinancialSchedule>
	fun findCandidates(userId: UUID, from: LocalDate, toInclusive: LocalDate): List<FinancialSchedule>
	fun delete(schedule: FinancialSchedule)
}
