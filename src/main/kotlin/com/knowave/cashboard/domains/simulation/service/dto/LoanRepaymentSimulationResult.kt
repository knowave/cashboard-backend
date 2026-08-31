package com.knowave.cashboard.domains.simulation.service.dto

import com.knowave.cashboard.domains.simulation.calculator.LoanRepaymentDifference
import com.knowave.cashboard.domains.simulation.calculator.LoanRepaymentSchedule
import com.knowave.cashboard.domains.simulation.policy.LiquidityAssessment
import java.time.LocalDate
import java.util.UUID

data class LoanRepaymentSimulationResult(
	val baseDate: LocalDate,
	val loanId: UUID,
	val requestedPrepaymentAmount: Long,
	val appliedPrepaymentAmount: Long,
	val prepaymentAmountAdjusted: Boolean,
	val liquidity: LiquidityAssessment,
	val current: LoanRepaymentSchedule,
	val simulated: LoanRepaymentSchedule,
	val difference: LoanRepaymentDifference,
)
