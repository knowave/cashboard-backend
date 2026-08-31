package com.knowave.cashboard.domains.simulation.service.dto

import java.util.UUID

data class LoanRepaymentSimulationCommand(
	val loanId: UUID,
	val prepaymentAmount: Long,
)
