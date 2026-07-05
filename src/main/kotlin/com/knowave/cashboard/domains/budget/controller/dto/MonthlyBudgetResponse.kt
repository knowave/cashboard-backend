package com.knowave.cashboard.domains.budget.controller.dto

import com.knowave.cashboard.domains.budget.entity.BudgetStatus
import com.knowave.cashboard.domains.budget.service.dto.MonthlyBudgetResult
import java.time.LocalDateTime
import java.util.UUID

data class MonthlyBudgetResponse(
	val id: UUID,
	val targetMonth: String,
	val monthlyBudget: Long,
	val usedAmount: Long,
	val remainingAmount: Long,
	val remainingDays: Int,
	val dailyAvailableAmount: Long,
	val weeklyAvailableAmount: Long,
	val status: BudgetStatus,
	val strategyMessage: String,
	val createdAt: LocalDateTime,
	val updatedAt: LocalDateTime,
)

fun MonthlyBudgetResult.toResponse(): MonthlyBudgetResponse = MonthlyBudgetResponse(
	id = id,
	targetMonth = targetMonth,
	monthlyBudget = monthlyBudget,
	usedAmount = usedAmount,
	remainingAmount = remainingAmount,
	remainingDays = remainingDays,
	dailyAvailableAmount = dailyAvailableAmount,
	weeklyAvailableAmount = weeklyAvailableAmount,
	status = status,
	strategyMessage = strategyMessage,
	createdAt = createdAt,
	updatedAt = updatedAt,
)
