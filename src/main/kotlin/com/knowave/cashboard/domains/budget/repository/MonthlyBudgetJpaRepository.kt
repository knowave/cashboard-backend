package com.knowave.cashboard.domains.budget.repository

import com.knowave.cashboard.domains.budget.entity.MonthlyBudget
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface MonthlyBudgetJpaRepository : JpaRepository<MonthlyBudget, UUID> {
	fun findByTargetMonth(targetMonth: String): MonthlyBudget?
	fun existsByTargetMonth(targetMonth: String): Boolean
}
