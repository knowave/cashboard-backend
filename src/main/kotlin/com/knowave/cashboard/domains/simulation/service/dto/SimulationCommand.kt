package com.knowave.cashboard.domains.simulation.service.dto

import java.time.YearMonth
import java.util.UUID

data class MonthlySimulationCommand(
	val from: YearMonth,
	val to: YearMonth,
	val monthlySalary: Long,
	val emergencyFund: Long,
	val savings: Long,
)

data class EarlyRepaymentSimulationCommand(
	val emergencyReserveThreshold: Long,
	val targetLoanId: UUID?,
	val desiredRepaymentAmount: Long?,
)
