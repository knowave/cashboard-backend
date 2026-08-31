package com.knowave.cashboard.domains.simulation.service

import com.knowave.cashboard.domains.simulation.calculator.LoanRepaymentCalculator
import com.knowave.cashboard.domains.simulation.context.SimulationContextProvider
import com.knowave.cashboard.domains.simulation.policy.EmergencyFundPolicy
import com.knowave.cashboard.domains.simulation.policy.EmergencyFundRecommendation
import com.knowave.cashboard.domains.simulation.service.dto.LoanRepaymentSimulationCommand
import com.knowave.cashboard.domains.simulation.service.dto.LoanRepaymentSimulationResult
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class SimulationFacade(
	private val contextProvider: SimulationContextProvider,
	private val emergencyFundPolicy: EmergencyFundPolicy,
	private val loanRepaymentCalculator: LoanRepaymentCalculator,
) {
	fun simulateLoanRepayment(command: LoanRepaymentSimulationCommand): LoanRepaymentSimulationResult =
		simulateLoanRepayment(command, recommendationOverride = null)

	internal fun simulateLoanRepayment(
		command: LoanRepaymentSimulationCommand,
		recommendationOverride: EmergencyFundRecommendation?,
	): LoanRepaymentSimulationResult {
		val context = contextProvider.loadLoanRepaymentContext(command.loanId)
		val liquidityContext = context.toLiquidityContext()
		val recommendation = recommendationOverride ?: emergencyFundPolicy.recommend(liquidityContext)
		val liquidity = emergencyFundPolicy.assess(
			context = liquidityContext,
			recommendation = recommendation,
			requestedPrepaymentAmount = command.prepaymentAmount,
			maximumRepaymentAmount = context.loan.currentBalance,
		)
		val comparison = loanRepaymentCalculator.calculate(
			loan = context.loan,
			prepaymentAmount = liquidity.appliedPrepaymentAmount,
			baseDate = context.baseDate,
		)

		return LoanRepaymentSimulationResult(
			baseDate = context.baseDate,
			loanId = context.loan.id,
			requestedPrepaymentAmount = command.prepaymentAmount,
			appliedPrepaymentAmount = liquidity.appliedPrepaymentAmount,
			prepaymentAmountAdjusted = liquidity.appliedPrepaymentAmount < command.prepaymentAmount,
			liquidity = liquidity,
			current = comparison.current,
			simulated = comparison.simulated,
			difference = comparison.difference,
		)
	}
}
