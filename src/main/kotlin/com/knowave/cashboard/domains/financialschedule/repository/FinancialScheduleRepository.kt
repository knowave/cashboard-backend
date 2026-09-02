package com.knowave.cashboard.domains.financialschedule.repository

import com.knowave.cashboard.domains.financialschedule.entity.FinancialSchedule
import java.time.LocalDate
import java.util.UUID

interface FinancialScheduleRepository {
	fun save(schedule: FinancialSchedule): FinancialSchedule
	fun findById(id: UUID): FinancialSchedule?
	fun findAllOrderByCreatedAtDesc(): List<FinancialSchedule>
	fun findCandidates(from: LocalDate, toInclusive: LocalDate): List<FinancialSchedule>
	fun delete(schedule: FinancialSchedule)
}
