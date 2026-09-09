package com.knowave.cashboard.domains.simulation.context

import java.util.UUID

interface SimulationContextProvider {
	fun loadLiquidityContext(userId: UUID): LiquidityContext
	fun loadLoanRepaymentContext(userId: UUID, loanId: UUID): SimulationContext
}
