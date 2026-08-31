package com.knowave.cashboard.domains.simulation.legacy

import com.knowave.cashboard.domains.simulation.calculator.LoanRepaymentCalculator
import com.knowave.cashboard.domains.simulation.context.LiquidityContext
import com.knowave.cashboard.domains.simulation.context.LoanSnapshot
import com.knowave.cashboard.domains.simulation.context.SimulationContext
import com.knowave.cashboard.domains.simulation.context.SimulationContextProvider
import com.knowave.cashboard.domains.simulation.policy.EmergencyFundPolicy
import com.knowave.cashboard.domains.simulation.service.SimulationFacade
import com.knowave.cashboard.domains.simulation.service.dto.EarlyRepaymentSimulationCommand
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class LegacyEarlyRepaymentAdapterTest {
	private val loanId = UUID.fromString("00000000-0000-0000-0000-000000000001")
	private val provider = StubProvider()
	private val policy = EmergencyFundPolicy()
	private val facade = SimulationFacade(provider, policy, LoanRepaymentCalculator())
	private val adapter = LegacyEarlyRepaymentAdapter(provider, policy, facade)

	@Test
	fun `대출 ID가 없어도 기존 유동성 응답을 반환한다`() {
		val result = adapter.simulate(
			EarlyRepaymentSimulationCommand(
				emergencyReserveThreshold = 4_000_000L,
				targetLoanId = null,
				desiredRepaymentAmount = null,
			),
		)

		assertThat(result.liquidCash).isEqualTo(5_000_000L)
		assertThat(result.emergencyReserveThreshold).isEqualTo(4_000_000L)
		assertThat(result.possibleRepaymentAmount).isEqualTo(1_000_000L)
		assertThat(result.executableRepaymentAmount).isEqualTo(1_000_000L)
		assertThat(result.targetLoanCurrentBalance).isNull()
	}

	@Test
	fun `대출 ID가 있으면 기존 임계값을 override로 사용하고 기존 필드로 축소한다`() {
		val result = adapter.simulate(
			EarlyRepaymentSimulationCommand(
				emergencyReserveThreshold = 4_000_000L,
				targetLoanId = loanId,
				desiredRepaymentAmount = 5_000_000L,
			),
		)

		assertThat(result.desiredRepaymentAmount).isEqualTo(5_000_000L)
		assertThat(result.possibleRepaymentAmount).isEqualTo(1_000_000L)
		assertThat(result.executableRepaymentAmount).isEqualTo(1_000_000L)
		assertThat(result.targetLoanCurrentBalance).isEqualTo(10_000_000L)
	}

	@Test
	fun `대출 ID가 있으면 기존 가능액과 대출 잔액 중 작은 금액만 실행한다`() {
		provider.loanBalance = 2_000_000L

		val result = adapter.simulate(
			EarlyRepaymentSimulationCommand(
				emergencyReserveThreshold = 0L,
				targetLoanId = loanId,
				desiredRepaymentAmount = null,
			),
		)

		assertThat(result.possibleRepaymentAmount).isEqualTo(5_000_000L)
		assertThat(result.executableRepaymentAmount).isEqualTo(2_000_000L)
		assertThat(result.targetLoanCurrentBalance).isEqualTo(2_000_000L)
	}

	private inner class StubProvider : SimulationContextProvider {
		var loanBalance = 10_000_000L

		private val liquidity = LiquidityContext(
			baseDate = LocalDate.of(2026, 8, 31),
			liquidAssetAmount = 5_000_000L,
			emergencyAssetAmount = 3_000_000L,
			averageMonthlyExpenseAmount = 1_000_000L,
			expenseHistoryMonthCount = 3,
		)

		override fun loadLiquidityContext(): LiquidityContext = liquidity

		override fun loadLoanRepaymentContext(loanId: UUID) = SimulationContext(
			baseDate = liquidity.baseDate,
			liquidAssetAmount = liquidity.liquidAssetAmount,
			emergencyAssetAmount = liquidity.emergencyAssetAmount,
			averageMonthlyExpenseAmount = liquidity.averageMonthlyExpenseAmount,
			expenseHistoryMonthCount = liquidity.expenseHistoryMonthCount,
			loan = LoanSnapshot(
				id = loanId,
				currentBalance = loanBalance,
				annualInterestRate = BigDecimal("6.0"),
				monthlyPaymentAmount = 500_000L,
			),
		)
	}
}
