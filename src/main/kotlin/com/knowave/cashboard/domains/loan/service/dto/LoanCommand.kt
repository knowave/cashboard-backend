package com.knowave.cashboard.domains.loan.service.dto

import com.knowave.cashboard.domains.loan.entity.Loan
import java.math.BigDecimal
import java.time.YearMonth

data class CreateLoanCommand(
	val principal: Long,
	val annualInterestRate: BigDecimal,
	val monthlyPayment: Long,
	val currentBalance: Long,
	val startMonth: YearMonth,
	val maturityMonth: YearMonth,
) {
	fun toEntity(): Loan = Loan(
		principal = principal,
		annualInterestRate = annualInterestRate,
		monthlyPayment = monthlyPayment,
		currentBalance = currentBalance,
		startMonth = startMonth.toString(),
		maturityMonth = maturityMonth.toString(),
	)
}

data class UpdateLoanCommand(
	val principal: Long,
	val annualInterestRate: BigDecimal,
	val monthlyPayment: Long,
	val currentBalance: Long,
	val startMonth: YearMonth,
	val maturityMonth: YearMonth,
)
