package com.knowave.cashboard.domains.dashboard.controller.dto

import com.knowave.cashboard.domains.dashboard.service.dto.DashboardResult

data class DashboardResponse(
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

fun DashboardResult.toResponse(): DashboardResponse = DashboardResponse(
	totalAccountBalance = totalAccountBalance,
	liquidCash = liquidCash,
	emergencyBalance = emergencyBalance,
	savingsBalance = savingsBalance,
	investmentBalance = investmentBalance,
	totalLoanBalance = totalLoanBalance,
	monthlyFixedExpense = monthlyFixedExpense,
	monthlyLoanPayment = monthlyLoanPayment,
	netWorth = netWorth,
	earlyRepaymentPossibleAmount = earlyRepaymentPossibleAmount,
	earlyRepaymentDecision = earlyRepaymentDecision,
	earlyRepaymentDecisionDescription = earlyRepaymentDecisionDescription,
)
