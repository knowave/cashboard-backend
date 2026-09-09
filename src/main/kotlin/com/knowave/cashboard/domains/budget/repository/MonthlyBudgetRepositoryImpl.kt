package com.knowave.cashboard.domains.budget.repository

import com.knowave.cashboard.domains.budget.entity.MonthlyBudget
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class MonthlyBudgetRepositoryImpl(
	private val monthlyBudgetJpaRepository: MonthlyBudgetJpaRepository,
) : MonthlyBudgetRepository {
	override fun save(monthlyBudget: MonthlyBudget): MonthlyBudget =
		monthlyBudgetJpaRepository.save(monthlyBudget)

	override fun findByIdAndUserId(id: UUID, userId: UUID): MonthlyBudget? =
		monthlyBudgetJpaRepository.findByIdAndUserId(id, userId)

	override fun findByIdForUpdate(id: UUID, userId: UUID): MonthlyBudget? =
		monthlyBudgetJpaRepository.findByIdForUpdate(id, userId)

	override fun findByTargetMonthAndUserId(targetMonth: String, userId: UUID): MonthlyBudget? =
		monthlyBudgetJpaRepository.findByTargetMonthAndUserId(targetMonth, userId)

	override fun existsByIdAndUserId(id: UUID, userId: UUID): Boolean =
		monthlyBudgetJpaRepository.existsByIdAndUserId(id, userId)

	override fun existsByTargetMonthAndUserId(targetMonth: String, userId: UUID): Boolean =
		monthlyBudgetJpaRepository.existsByTargetMonthAndUserId(targetMonth, userId)
}
