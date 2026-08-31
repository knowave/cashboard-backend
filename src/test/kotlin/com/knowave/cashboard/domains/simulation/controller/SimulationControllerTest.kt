package com.knowave.cashboard.domains.simulation.controller

import com.knowave.cashboard.common.exception.NotFoundException
import com.knowave.cashboard.domains.simulation.calculator.LoanRepaymentDifference
import com.knowave.cashboard.domains.simulation.calculator.LoanRepaymentSchedule
import com.knowave.cashboard.domains.simulation.policy.EmergencyFundBasis
import com.knowave.cashboard.domains.simulation.policy.LiquidityAssessment
import com.knowave.cashboard.domains.simulation.service.SimulationFacade
import com.knowave.cashboard.domains.simulation.service.SimulationService
import com.knowave.cashboard.domains.simulation.service.dto.EarlyRepaymentSimulationCommand
import com.knowave.cashboard.domains.simulation.service.dto.EarlyRepaymentSimulationResult
import com.knowave.cashboard.domains.simulation.service.dto.LoanRepaymentSimulationCommand
import com.knowave.cashboard.domains.simulation.service.dto.LoanRepaymentSimulationResult
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

@WebMvcTest(SimulationController::class)
class SimulationControllerTest {
	@Autowired private lateinit var mockMvc: MockMvc
	@MockitoBean private lateinit var simulationService: SimulationService
	@MockitoBean private lateinit var simulationFacade: SimulationFacade

	@Test
	fun `대출 조기상환 상세 결과를 반환한다`() {
		given(simulationFacade.simulateLoanRepayment(loanRepaymentCommand(5_000_000L)))
			.willReturn(result())

		mockMvc.perform(
			post("/simulations/loan-repayment")
				.contentType(MediaType.APPLICATION_JSON)
				.content(
					"""
					{
					  "loanId": "00000000-0000-0000-0000-000000000001",
					  "prepaymentAmount": 5000000
					}
					""".trimIndent(),
				),
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.appliedPrepaymentAmount").value(3500000))
			.andExpect(jsonPath("$.data.prepaymentAmountAdjusted").value(true))
			.andExpect(jsonPath("$.data.liquidity.emergencyFundBasis").value("RECENT_EXPENSE_AVERAGE"))
			.andExpect(jsonPath("$.data.current.remainingPrincipalAmount").value(10000000))
	}

	@Test
	fun `loanId가 없으면 400을 반환한다`() {
		mockMvc.perform(
			post("/simulations/loan-repayment")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""{"prepaymentAmount": 1000000}"""),
		).andExpect(status().isBadRequest)
	}

	@Test
	fun `조기상환액이 0이면 400을 반환한다`() {
		mockMvc.perform(
			post("/simulations/loan-repayment")
				.contentType(MediaType.APPLICATION_JSON)
				.content(
					"""
					{
					  "loanId": "00000000-0000-0000-0000-000000000001",
					  "prepaymentAmount": 0
					}
					""".trimIndent(),
				),
		).andExpect(status().isBadRequest)
	}

	@Test
	fun `존재하지 않는 대출이면 404를 반환한다`() {
		val loanId = UUID.fromString("00000000-0000-0000-0000-000000000001")
		given(simulationFacade.simulateLoanRepayment(loanRepaymentCommand(1_000_000L)))
			.willThrow(NotFoundException("Loan", loanId))

		mockMvc.perform(
			post("/simulations/loan-repayment")
				.contentType(MediaType.APPLICATION_JSON)
				.content(
					"""
					{
					  "loanId": "00000000-0000-0000-0000-000000000001",
					  "prepaymentAmount": 1000000
					}
					""".trimIndent(),
				),
		)
			.andExpect(status().isNotFound)
			.andExpect(jsonPath("$.data.code").value("NOT_FOUND"))
	}

	@Test
	fun `기존 early repayment 경로와 응답 필드를 유지한다`() {
		given(
			simulationService.simulateEarlyRepayment(
				EarlyRepaymentSimulationCommand(
					emergencyReserveThreshold = 4_000_000L,
					targetLoanId = UUID.fromString("00000000-0000-0000-0000-000000000001"),
					desiredRepaymentAmount = 5_000_000L,
				),
			),
		)
			.willReturn(
				EarlyRepaymentSimulationResult(
					liquidCash = 5_000_000L,
					emergencyReserveThreshold = 4_000_000L,
					possibleRepaymentAmount = 1_000_000L,
					desiredRepaymentAmount = 5_000_000L,
					executableRepaymentAmount = 1_000_000L,
					targetLoanCurrentBalance = 10_000_000L,
					decision = "PROHIBITED",
					decisionDescription = "상환 금지",
				),
			)

		mockMvc.perform(
			post("/simulations/early-repayment")
				.contentType(MediaType.APPLICATION_JSON)
				.content(
					"""
					{
					  "emergencyReserveThreshold": 4000000,
					  "targetLoanId": "00000000-0000-0000-0000-000000000001",
					  "desiredRepaymentAmount": 5000000
					}
					""".trimIndent(),
				),
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.data.liquidCash").value(5000000))
			.andExpect(jsonPath("$.data.emergencyReserveThreshold").value(4000000))
			.andExpect(jsonPath("$.data.possibleRepaymentAmount").value(1000000))
			.andExpect(jsonPath("$.data.executableRepaymentAmount").value(1000000))
	}

	private fun result(): LoanRepaymentSimulationResult {
		val schedule = LoanRepaymentSchedule(
			remainingPrincipalAmount = 10_000_000L,
			monthlyPaymentAmount = 500_000L,
			remainingMonths = 21,
			estimatedTotalInterestAmount = 500_000L,
			estimatedTotalPaymentAmount = 10_500_000L,
			estimatedPayoffMonth = YearMonth.of(2028, 5),
		)
		return LoanRepaymentSimulationResult(
			baseDate = LocalDate.of(2026, 8, 31),
			loanId = UUID.fromString("00000000-0000-0000-0000-000000000001"),
			requestedPrepaymentAmount = 5_000_000L,
			appliedPrepaymentAmount = 3_500_000L,
			prepaymentAmountAdjusted = true,
			liquidity = LiquidityAssessment(
				liquidAssetAmount = 5_000_000L,
				emergencyAssetAmount = 3_000_000L,
				cashEquivalentAssetAmount = 8_000_000L,
				averageMonthlyExpenseAmount = 1_500_000L,
				expenseHistoryMonthCount = 3,
				emergencyFundBasis = EmergencyFundBasis.RECENT_EXPENSE_AVERAGE,
				coverageMonths = 3,
				recommendedEmergencyFundAmount = 4_500_000L,
				availableRepaymentAmount = 5_000_000L,
				safeRepaymentLimit = 3_500_000L,
				requestedPrepaymentAmount = 5_000_000L,
				appliedPrepaymentAmount = 3_500_000L,
				remainingLiquidAssetAmount = 1_500_000L,
				remainingCashEquivalentAssetAmount = 4_500_000L,
				safe = true,
			),
			current = schedule,
			simulated = schedule.copy(remainingPrincipalAmount = 6_500_000L, remainingMonths = 14),
			difference = LoanRepaymentDifference(savedInterestAmount = 200_000L, reducedMonths = 8),
		)
	}

	private fun loanRepaymentCommand(prepaymentAmount: Long): LoanRepaymentSimulationCommand =
		LoanRepaymentSimulationCommand(
			loanId = UUID.fromString("00000000-0000-0000-0000-000000000001"),
			prepaymentAmount = prepaymentAmount,
		)
}
