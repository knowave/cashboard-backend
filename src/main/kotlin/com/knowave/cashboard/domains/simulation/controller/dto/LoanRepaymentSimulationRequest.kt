package com.knowave.cashboard.domains.simulation.controller.dto

import com.knowave.cashboard.domains.simulation.service.dto.LoanRepaymentSimulationCommand
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.util.UUID

data class LoanRepaymentSimulationRequest(
	@field:NotNull(message = "loanId is required.")
	val loanId: UUID? = null,

	@field:Positive(message = "prepaymentAmount must be greater than 0.")
	val prepaymentAmount: Long = 0L,
) {
	fun toCommand(): LoanRepaymentSimulationCommand = LoanRepaymentSimulationCommand(
		loanId = requireNotNull(loanId),
		prepaymentAmount = prepaymentAmount,
	)
}
