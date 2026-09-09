package com.knowave.cashboard.domains.fixedexpense.repository

import com.knowave.cashboard.domains.fixedexpense.entity.FixedExpense
import java.util.UUID

interface FixedExpenseRepository {
	fun save(fixedExpense: FixedExpense): FixedExpense
	fun findByIdAndUserId(id: UUID, userId: UUID): FixedExpense?
	fun findAllByUserId(userId: UUID): List<FixedExpense>
	fun delete(fixedExpense: FixedExpense)
}
