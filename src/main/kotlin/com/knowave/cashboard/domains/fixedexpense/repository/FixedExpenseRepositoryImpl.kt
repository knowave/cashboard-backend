package com.knowave.cashboard.domains.fixedexpense.repository

import com.knowave.cashboard.domains.fixedexpense.entity.FixedExpense
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class FixedExpenseRepositoryImpl(
	private val fixedExpenseJpaRepository: FixedExpenseJpaRepository,
) : FixedExpenseRepository {
	override fun save(fixedExpense: FixedExpense): FixedExpense = fixedExpenseJpaRepository.save(fixedExpense)

	override fun findById(id: UUID): FixedExpense? = fixedExpenseJpaRepository.findById(id).orElse(null)

	override fun findAll(): List<FixedExpense> = fixedExpenseJpaRepository.findAll()

	override fun delete(fixedExpense: FixedExpense) = fixedExpenseJpaRepository.delete(fixedExpense)
}
