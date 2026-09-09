package com.knowave.cashboard.domains.financialschedule.repository

import com.knowave.cashboard.domains.financialschedule.entity.FinancialSchedule
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.util.UUID

interface FinancialScheduleJpaRepository : JpaRepository<FinancialSchedule, UUID> {
	fun findByIdAndUserId(id: UUID, userId: UUID): FinancialSchedule?

	fun findAllByUserIdOrderByCreatedAtDescIdAsc(userId: UUID): List<FinancialSchedule>

	@Query(
		"""
		select schedule from FinancialSchedule schedule
		where schedule.userId = :userId
		  and (
		      schedule.scheduledDate between :from and :toInclusive
		      or (
		          schedule.startDate <= :toInclusive
		          and (schedule.endDate is null or schedule.endDate >= :from)
		      )
		  )
		""",
	)
	fun findCandidates(
		@Param("userId") userId: UUID,
		@Param("from") from: LocalDate,
		@Param("toInclusive") toInclusive: LocalDate,
	): List<FinancialSchedule>
}
