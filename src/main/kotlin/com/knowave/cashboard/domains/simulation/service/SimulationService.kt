package com.knowave.cashboard.domains.simulation.service

import com.knowave.cashboard.domains.simulation.service.dto.EarlyRepaymentSimulationCommand
import com.knowave.cashboard.domains.simulation.service.dto.EarlyRepaymentSimulationResult
import com.knowave.cashboard.domains.simulation.service.dto.MonthlyCashFlowResult
import com.knowave.cashboard.domains.simulation.service.dto.MonthlySimulationCommand

interface SimulationService {
	fun simulateMonthly(command: MonthlySimulationCommand): List<MonthlyCashFlowResult>
	fun simulateEarlyRepayment(command: EarlyRepaymentSimulationCommand): EarlyRepaymentSimulationResult
}
