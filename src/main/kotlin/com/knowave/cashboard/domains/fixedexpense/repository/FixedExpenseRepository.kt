package com.knowave.cashboard.domains.fixedexpense.repository

import com.knowave.cashboard.domains.fixedexpense.entity.FixedExpense
import java.util.UUID

interface FixedExpenseRepository {
	fun save(fixedExpense: FixedExpense): FixedExpense
	fun findById(id: UUID): FixedExpense?
	fun findAll(): List<FixedExpense>
	fun delete(fixedExpense: FixedExpense)
}
