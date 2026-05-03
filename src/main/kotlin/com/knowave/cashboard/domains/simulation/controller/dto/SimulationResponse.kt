package com.knowave.cashboard.domains.simulation.controller.dto

import com.knowave.cashboard.domains.simulation.service.dto.EarlyRepaymentSimulationResult
import com.knowave.cashboard.domains.simulation.service.dto.MonthlyCashFlowResult

data class MonthlySimulationResponse(
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

data class EarlyRepaymentSimulationResponse(
	val liquidCash: Long,
	val emergencyReserveThreshold: Long,
	val possibleRepaymentAmount: Long,
	val desiredRepaymentAmount: Long?,
	val executableRepaymentAmount: Long,
	val targetLoanCurrentBalance: Long?,
	val decision: String,
	val decisionDescription: String,
)

fun MonthlyCashFlowResult.toResponse(): MonthlySimulationResponse = MonthlySimulationResponse(
	month = month,
	salary = salary,
	fixedExpense = fixedExpense,
	emergencyFund = emergencyFund,
	savings = savings,
	loanPayment = loanPayment,
	availableLivingExpense = availableLivingExpense,
	netCashFlow = netCashFlow,
	estimatedLoanBalance = estimatedLoanBalance,
)

fun EarlyRepaymentSimulationResult.toResponse(): EarlyRepaymentSimulationResponse = EarlyRepaymentSimulationResponse(
	liquidCash = liquidCash,
	emergencyReserveThreshold = emergencyReserveThreshold,
	possibleRepaymentAmount = possibleRepaymentAmount,
	desiredRepaymentAmount = desiredRepaymentAmount,
	executableRepaymentAmount = executableRepaymentAmount,
	targetLoanCurrentBalance = targetLoanCurrentBalance,
	decision = decision,
	decisionDescription = decisionDescription,
)
