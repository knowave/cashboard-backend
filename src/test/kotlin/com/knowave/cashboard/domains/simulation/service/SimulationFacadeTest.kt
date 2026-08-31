package com.knowave.cashboard.domains.simulation.service

import com.knowave.cashboard.domains.simulation.calculator.LoanRepaymentCalculator
import com.knowave.cashboard.domains.simulation.context.LiquidityContext
import com.knowave.cashboard.domains.simulation.context.LoanSnapshot
import com.knowave.cashboard.domains.simulation.context.SimulationContext
import com.knowave.cashboard.domains.simulation.context.SimulationContextProvider
import com.knowave.cashboard.domains.simulation.policy.EmergencyFundBasis
import com.knowave.cashboard.domains.simulation.policy.EmergencyFundPolicy
import com.knowave.cashboard.domains.simulation.service.dto.LoanRepaymentSimulationCommand
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class SimulationFacadeTest {
	private val loanId = UUID.fromString("00000000-0000-0000-0000-000000000001")
	private val provider = StubContextProvider(context())
	private val facade = SimulationFacade(provider, EmergencyFundPolicy(), LoanRepaymentCalculator())

	@Test
	fun `Context 정책 계산기 결과를 상세 결과로 조합한다`() {
		val result = facade.simulateLoanRepayment(
			LoanRepaymentSimulationCommand(loanId = loanId, prepaymentAmount = 5_000_000L),
		)

		assertThat(provider.requestedLoanId).isEqualTo(loanId)
		assertThat(result.requestedPrepaymentAmount).isEqualTo(5_000_000L)
		assertThat(result.appliedPrepaymentAmount).isEqualTo(3_500_000L)
		assertThat(result.prepaymentAmountAdjusted).isTrue()
		assertThat(result.liquidity.emergencyFundBasis)
			.isEqualTo(EmergencyFundBasis.RECENT_EXPENSE_AVERAGE)
		assertThat(result.current.remainingPrincipalAmount).isEqualTo(10_000_000L)
		assertThat(result.simulated.remainingPrincipalAmount).isEqualTo(6_500_000L)
		assertThat(result.difference.savedInterestAmount).isGreaterThan(0L)
	}

	private class StubContextProvider(private val value: SimulationContext) : SimulationContextProvider {
		var requestedLoanId: UUID? = null

		override fun loadLiquidityContext(): LiquidityContext = value.toLiquidityContext()

		override fun loadLoanRepaymentContext(loanId: UUID): SimulationContext {
			requestedLoanId = loanId
			return value
		}
	}

	private companion object {
		fun context() = SimulationContext(
			baseDate = LocalDate.of(2026, 8, 31),
			liquidAssetAmount = 5_000_000L,
			emergencyAssetAmount = 3_000_000L,
			averageMonthlyExpenseAmount = 1_500_000L,
			expenseHistoryMonthCount = 3,
			loan = LoanSnapshot(
				id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
				currentBalance = 10_000_000L,
				annualInterestRate = BigDecimal("6.0"),
				monthlyPaymentAmount = 500_000L,
			),
		)
	}
}
