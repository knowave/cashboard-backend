package com.knowave.cashboard.domains.loan.service.dto

import com.knowave.cashboard.domains.loan.entity.Loan
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class LoanResult(
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

fun Loan.toResult(): LoanResult = LoanResult(
	id = requireNotNull(id),
	principal = principal,
	annualInterestRate = annualInterestRate,
	monthlyPayment = monthlyPayment,
	currentBalance = currentBalance,
	startMonth = startMonth,
	maturityMonth = maturityMonth,
	createdAt = createdAt,
	updatedAt = updatedAt,
)
