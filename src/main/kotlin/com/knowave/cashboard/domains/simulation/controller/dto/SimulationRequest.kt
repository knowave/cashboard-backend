package com.knowave.cashboard.domains.simulation.controller.dto

import com.knowave.cashboard.domains.simulation.service.dto.EarlyRepaymentSimulationCommand
import com.knowave.cashboard.domains.simulation.service.dto.MonthlySimulationCommand
import jakarta.validation.constraints.Min
import java.time.YearMonth
import java.util.UUID

data class EarlyRepaymentSimulationRequest(
	@field:Min(value = 0, message = "emergencyReserveThreshold must be greater than or equal to 0.")
	val emergencyReserveThreshold: Long = 5_000_000L,

	val targetLoanId: UUID? = null,

	@field:Min(value = 0, message = "desiredRepaymentAmount must be greater than or equal to 0.")
	val desiredRepaymentAmount: Long? = null,
) {
	fun toCommand(): EarlyRepaymentSimulationCommand = EarlyRepaymentSimulationCommand(
		emergencyReserveThreshold = emergencyReserveThreshold,
		targetLoanId = targetLoanId,
		desiredRepaymentAmount = desiredRepaymentAmount,
	)
}

fun monthlySimulationCommand(
	from: String,
	to: String,
	monthlySalary: Long,
	emergencyFund: Long,
	savings: Long,
): MonthlySimulationCommand = MonthlySimulationCommand(
	from = YearMonth.parse(from),
	to = YearMonth.parse(to),
	monthlySalary = monthlySalary,
	emergencyFund = emergencyFund,
	savings = savings,
)
