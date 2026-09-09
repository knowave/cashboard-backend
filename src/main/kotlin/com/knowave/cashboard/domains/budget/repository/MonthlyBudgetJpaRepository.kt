package com.knowave.cashboard.domains.budget.repository

import com.knowave.cashboard.domains.budget.entity.MonthlyBudget
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface MonthlyBudgetJpaRepository : JpaRepository<MonthlyBudget, UUID> {
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query(
		"SELECT monthlyBudget FROM MonthlyBudget monthlyBudget " +
			"WHERE monthlyBudget.id = :id AND monthlyBudget.userId = :userId",
	)
	fun findByIdForUpdate(@Param("id") id: UUID, @Param("userId") userId: UUID): MonthlyBudget?

	fun findByIdAndUserId(id: UUID, userId: UUID): MonthlyBudget?
	fun findByTargetMonthAndUserId(targetMonth: String, userId: UUID): MonthlyBudget?
	fun existsByIdAndUserId(id: UUID, userId: UUID): Boolean
	fun existsByTargetMonthAndUserId(targetMonth: String, userId: UUID): Boolean
}
