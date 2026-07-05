package com.knowave.cashboard.domains.budget.repository

import com.knowave.cashboard.domains.budget.entity.MonthlyBudget
import java.util.UUID

interface MonthlyBudgetRepository {
	fun save(monthlyBudget: MonthlyBudget): MonthlyBudget
	fun findById(id: UUID): MonthlyBudget?
	fun findByTargetMonth(targetMonth: String): MonthlyBudget?
	fun existsById(id: UUID): Boolean
	fun existsByTargetMonth(targetMonth: String): Boolean
}
