package com.knowave.cashboard.domains.loan.entity

import com.knowave.cashboard.common.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.YearMonth

@Entity
@Table(name = "loans")
class Loan(
	@Column(name = "principal", nullable = false)
	var principal: Long,

	@Column(name = "annual_interest_rate", nullable = false, precision = 6, scale = 3)
	var annualInterestRate: BigDecimal,

	@Column(name = "monthly_payment", nullable = false)
	var monthlyPayment: Long,

	@Column(name = "current_balance", nullable = false)
	var currentBalance: Long,

	@Column(name = "start_month", nullable = false, length = 7)
	var startMonth: String,

	@Column(name = "maturity_month", nullable = false, length = 7)
	var maturityMonth: String,
) : BaseEntity() {
	fun update(
		principal: Long,
		annualInterestRate: BigDecimal,
		monthlyPayment: Long,
		currentBalance: Long,
		startMonth: YearMonth,
		maturityMonth: YearMonth,
	) {
		this.principal = principal
		this.annualInterestRate = annualInterestRate
		this.monthlyPayment = monthlyPayment
		this.currentBalance = currentBalance
		this.startMonth = startMonth.toString()
		this.maturityMonth = maturityMonth.toString()
	}
}
