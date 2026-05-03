package com.knowave.cashboard.domains.loan.controller.dto

import com.knowave.cashboard.domains.loan.service.dto.LoanResult
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class LoanResponse(
	val id: UUID,
	val principal: Long,
	val annualInterestRate: BigDecimal,
	val monthlyPayment: Long,
	val currentBalance: Long,
	val startMonth: String,
	val maturityMonth: String,
	val createdAt: LocalDateTime?,
	val updatedAt: LocalDateTime?,
)

fun LoanResult.toResponse(): LoanResponse = LoanResponse(
	id = id,
	principal = principal,
	annualInterestRate = annualInterestRate,
	monthlyPayment = monthlyPayment,
	currentBalance = currentBalance,
	startMonth = startMonth,
	maturityMonth = maturityMonth,
	createdAt = createdAt,
	updatedAt = updatedAt,
)
