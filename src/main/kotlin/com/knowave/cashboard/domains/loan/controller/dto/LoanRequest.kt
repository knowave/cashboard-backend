package com.knowave.cashboard.domains.loan.controller.dto

import com.knowave.cashboard.domains.loan.service.dto.CreateLoanCommand
import com.knowave.cashboard.domains.loan.service.dto.UpdateLoanCommand
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Pattern
import java.math.BigDecimal
import java.time.YearMonth

data class LoanRequest(
	@field:Min(value = 0, message = "principal must be greater than or equal to 0.")
	val principal: Long,

	@field:DecimalMin(value = "0.0", message = "annualInterestRate must be greater than or equal to 0.")
	val annualInterestRate: BigDecimal,

	@field:Min(value = 0, message = "monthlyPayment must be greater than or equal to 0.")
	val monthlyPayment: Long,

	@field:Min(value = 0, message = "currentBalance must be greater than or equal to 0.")
	val currentBalance: Long,

	@field:Pattern(regexp = "\\d{4}-\\d{2}", message = "startMonth must be yyyy-MM.")
	val startMonth: String,

	@field:Pattern(regexp = "\\d{4}-\\d{2}", message = "maturityMonth must be yyyy-MM.")
	val maturityMonth: String,
) {
	fun toCreateCommand(): CreateLoanCommand = CreateLoanCommand(
		principal = principal,
		annualInterestRate = annualInterestRate,
		monthlyPayment = monthlyPayment,
		currentBalance = currentBalance,
		startMonth = YearMonth.parse(startMonth),
		maturityMonth = YearMonth.parse(maturityMonth),
	)

	fun toUpdateCommand(): UpdateLoanCommand = UpdateLoanCommand(
		principal = principal,
		annualInterestRate = annualInterestRate,
		monthlyPayment = monthlyPayment,
		currentBalance = currentBalance,
		startMonth = YearMonth.parse(startMonth),
		maturityMonth = YearMonth.parse(maturityMonth),
	)
}
