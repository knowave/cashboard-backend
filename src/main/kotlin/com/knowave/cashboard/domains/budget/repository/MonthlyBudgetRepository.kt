package com.knowave.cashboard.domains.budget.repository

import com.knowave.cashboard.domains.budget.entity.MonthlyBudget
import java.util.UUID

interface MonthlyBudgetRepository {
	fun save(monthlyBudget: MonthlyBudget): MonthlyBudget
	fun findByIdAndUserId(id: UUID, userId: UUID): MonthlyBudget?
	fun findByIdForUpdate(id: UUID, userId: UUID): MonthlyBudget?
	fun findByTargetMonthAndUserId(targetMonth: String, userId: UUID): MonthlyBudget?
	fun existsByIdAndUserId(id: UUID, userId: UUID): Boolean
	fun existsByTargetMonthAndUserId(targetMonth: String, userId: UUID): Boolean
}
