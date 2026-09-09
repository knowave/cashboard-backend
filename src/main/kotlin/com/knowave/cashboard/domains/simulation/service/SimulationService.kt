package com.knowave.cashboard.domains.simulation.service

import com.knowave.cashboard.domains.simulation.service.dto.EarlyRepaymentSimulationCommand
import com.knowave.cashboard.domains.simulation.service.dto.EarlyRepaymentSimulationResult
import com.knowave.cashboard.domains.simulation.service.dto.MonthlyCashFlowResult
import com.knowave.cashboard.domains.simulation.service.dto.MonthlySimulationCommand
import java.util.UUID

interface SimulationService {
	fun simulateMonthly(userId: UUID, command: MonthlySimulationCommand): List<MonthlyCashFlowResult>
	fun simulateEarlyRepayment(userId: UUID, command: EarlyRepaymentSimulationCommand): EarlyRepaymentSimulationResult
}
