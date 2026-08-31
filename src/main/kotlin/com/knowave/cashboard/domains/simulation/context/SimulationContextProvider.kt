package com.knowave.cashboard.domains.simulation.context

import java.util.UUID

interface SimulationContextProvider {
	fun loadLiquidityContext(): LiquidityContext
	fun loadLoanRepaymentContext(loanId: UUID): SimulationContext
}
