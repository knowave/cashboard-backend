package com.knowave.cashboard.domains.financialschedule.repository

import com.knowave.cashboard.domains.financialschedule.entity.FinancialSchedule
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.util.UUID

interface FinancialScheduleJpaRepository : JpaRepository<FinancialSchedule, UUID> {
	fun findAllByOrderByCreatedAtDescIdAsc(): List<FinancialSchedule>

	@Query(
		"""
		select schedule from FinancialSchedule schedule
		where schedule.scheduledDate between :from and :toInclusive
		   or (
		       schedule.startDate <= :toInclusive
		       and (schedule.endDate is null or schedule.endDate >= :from)
		   )
		""",
	)
	fun findCandidates(
		@Param("from") from: LocalDate,
		@Param("toInclusive") toInclusive: LocalDate,
	): List<FinancialSchedule>
}
