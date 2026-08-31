package com.knowave.cashboard.domains.simulation.legacy

import com.knowave.cashboard.domains.simulation.context.SimulationContextProvider
import com.knowave.cashboard.domains.simulation.entity.EarlyRepaymentDecision
import com.knowave.cashboard.domains.simulation.policy.EmergencyFundPolicy
import com.knowave.cashboard.domains.simulation.service.SimulationFacade
import com.knowave.cashboard.domains.simulation.service.dto.EarlyRepaymentSimulationCommand
import com.knowave.cashboard.domains.simulation.service.dto.EarlyRepaymentSimulationResult
import com.knowave.cashboard.domains.simulation.service.dto.LoanRepaymentSimulationCommand
import org.springframework.stereotype.Component

@Component
class LegacyEarlyRepaymentAdapter(
	private val contextProvider: SimulationContextProvider,
	private val emergencyFundPolicy: EmergencyFundPolicy,
	private val simulationFacade: SimulationFacade,
) {
	fun simulate(command: EarlyRepaymentSimulationCommand): EarlyRepaymentSimulationResult {
		val recommendation = emergencyFundPolicy.legacyRecommendation(command.emergencyReserveThreshold)
		val detailed = command.targetLoanId?.let { loanId ->
			simulationFacade.simulateLoanRepayment(
				command = LoanRepaymentSimulationCommand(
					loanId = loanId,
					prepaymentAmount = command.desiredRepaymentAmount ?: Long.MAX_VALUE,
				),
				recommendationOverride = recommendation,
			)
		}
		val assessment = detailed?.liquidity ?: emergencyFundPolicy.assess(
			context = contextProvider.loadLiquidityContext(),
			recommendation = recommendation,
			requestedPrepaymentAmount = command.desiredRepaymentAmount ?: Long.MAX_VALUE,
			maximumRepaymentAmount = Long.MAX_VALUE,
		)
		val legacyPossibleAmount = (assessment.liquidAssetAmount - command.emergencyReserveThreshold).coerceAtLeast(0L)
		val targetLoanCurrentBalance = detailed?.current?.remainingPrincipalAmount
		val executableAmount = listOfNotNull(
			legacyPossibleAmount,
			command.desiredRepaymentAmount,
			targetLoanCurrentBalance,
		).minOrNull() ?: legacyPossibleAmount
		val decision = EarlyRepaymentDecision.fromAvailableAmount(legacyPossibleAmount)

		return EarlyRepaymentSimulationResult(
			liquidCash = assessment.liquidAssetAmount,
			emergencyReserveThreshold = command.emergencyReserveThreshold,
			possibleRepaymentAmount = legacyPossibleAmount,
			desiredRepaymentAmount = command.desiredRepaymentAmount,
			executableRepaymentAmount = executableAmount,
			targetLoanCurrentBalance = targetLoanCurrentBalance,
			decision = decision.name,
			decisionDescription = decision.description,
		)
	}
}
