package com.knowave.cashboard.domains.fixedexpense.service.dto

import com.knowave.cashboard.domains.fixedexpense.entity.FixedExpense
import java.time.LocalDateTime
import java.util.UUID

data class FixedExpenseResult(
	val id: UUID,
	val name: String,
	val amount: Long,
	val category: String,
	val startMonth: String,
	val endMonth: String?,
	val createdAt: LocalDateTime?,
	val updatedAt: LocalDateTime?,
)

fun FixedExpense.toResult(): FixedExpenseResult = FixedExpenseResult(
	id = requireNotNull(id),
	name = name,
	amount = amount,
	category = category,
	startMonth = startMonth,
	endMonth = endMonth,
	createdAt = createdAt,
	updatedAt = updatedAt,
)
