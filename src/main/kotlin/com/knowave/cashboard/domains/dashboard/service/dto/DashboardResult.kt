package com.knowave.cashboard.domains.dashboard.service.dto

data class DashboardResult(
	val totalAccountBalance: Long,
	val liquidCash: Long,
	val emergencyBalance: Long,
	val savingsBalance: Long,
	val investmentBalance: Long,
	val totalLoanBalance: Long,
	val monthlyFixedExpense: Long,
	val monthlyLoanPayment: Long,
	val netWorth: Long,
	val earlyRepaymentPossibleAmount: Long,
	val earlyRepaymentDecision: String,
	val earlyRepaymentDecisionDescription: String,
)
