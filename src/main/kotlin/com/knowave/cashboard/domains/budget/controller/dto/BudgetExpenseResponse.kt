package com.knowave.cashboard.domains.budget.controller.dto

import com.knowave.cashboard.domains.budget.service.dto.BudgetExpenseResult
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class BudgetExpenseResponse(
	val id: UUID,
	val monthlyBudgetId: UUID,
	val amount: Long,
	val category: String?,
	val memo: String?,
	val spentAt: LocalDate,
	val createdAt: LocalDateTime,
	val updatedAt: LocalDateTime,
)

fun BudgetExpenseResult.toResponse(): BudgetExpenseResponse = BudgetExpenseResponse(
	id = id,
	monthlyBudgetId = monthlyBudgetId,
	amount = amount,
	category = category,
	memo = memo,
	spentAt = spentAt,
	createdAt = createdAt,
	updatedAt = updatedAt,
)
