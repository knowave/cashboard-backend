package com.knowave.cashboard.domains.fixedexpense.controller.dto

import com.knowave.cashboard.domains.fixedexpense.service.dto.FixedExpenseResult
import java.time.LocalDateTime
import java.util.UUID

data class FixedExpenseResponse(
	val id: UUID,
	val name: String,
	val amount: Long,
	val category: String,
	val startMonth: String,
	val endMonth: String?,
	val createdAt: LocalDateTime?,
	val updatedAt: LocalDateTime?,
)

fun FixedExpenseResult.toResponse(): FixedExpenseResponse = FixedExpenseResponse(
	id = id,
	name = name,
	amount = amount,
	category = category,
	startMonth = startMonth,
	endMonth = endMonth,
	createdAt = createdAt,
	updatedAt = updatedAt,
)
