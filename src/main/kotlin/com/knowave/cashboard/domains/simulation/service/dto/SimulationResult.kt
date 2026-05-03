package com.knowave.cashboard.domains.simulation.service.dto

data class MonthlyCashFlowResult(
	val month: String,
	val salary: Long,
	val fixedExpense: Long,
	val emergencyFund: Long,
	val savings: Long,
	val loanPayment: Long,
	val availableLivingExpense: Long,
	val netCashFlow: Long,
	val estimatedLoanBalance: Long,
)

data class EarlyRepaymentSimulationResult(
	val liquidCash: Long,
	val emergencyReserveThreshold: Long,
	val possibleRepaymentAmount: Long,
	val desiredRepaymentAmount: Long?,
	val executableRepaymentAmount: Long,
	val targetLoanCurrentBalance: Long?,
	val decision: String,
	val decisionDescription: String,
)
