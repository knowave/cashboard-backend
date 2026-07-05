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

	override fun findById(id: UUID): MonthlyBudget? =
		monthlyBudgetJpaRepository.findById(id).orElse(null)

	override fun findByTargetMonth(targetMonth: String): MonthlyBudget? =
		monthlyBudgetJpaRepository.findByTargetMonth(targetMonth)

	override fun existsById(id: UUID): Boolean =
		monthlyBudgetJpaRepository.existsById(id)

	override fun existsByTargetMonth(targetMonth: String): Boolean =
		monthlyBudgetJpaRepository.existsByTargetMonth(targetMonth)
}
