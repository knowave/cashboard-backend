package com.knowave.cashboard.domains.simulation.policy

import com.knowave.cashboard.domains.simulation.context.LiquidityContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class EmergencyFundPolicyTest {
	private val policy = EmergencyFundPolicy()

	@Test
	fun `최근 지출 이력이 있으면 평균 지출의 3개월치를 권장한다`() {
		val context = liquidity(averageExpense = 1_400_000L, historyMonths = 2)

		val result = policy.recommend(context)

		assertThat(result.amount).isEqualTo(4_200_000L)
		assertThat(result.basis).isEqualTo(EmergencyFundBasis.RECENT_EXPENSE_AVERAGE)
		assertThat(result.coverageMonths).isEqualTo(3)
	}

	@Test
	fun `지출 이력이 없으면 500만원 정책값을 사용한다`() {
		val result = policy.recommend(liquidity(averageExpense = 0L, historyMonths = 0))

		assertThat(result.amount).isEqualTo(5_000_000L)
		assertThat(result.basis).isEqualTo(EmergencyFundBasis.FALLBACK_POLICY)
	}

	@Test
	fun `비상계좌는 안전성에는 포함하지만 상환 재원에는 포함하지 않는다`() {
		val context = liquidity(liquid = 5_000_000L, emergency = 3_000_000L)
		val recommendation = EmergencyFundRecommendation(
			amount = 4_000_000L,
			basis = EmergencyFundBasis.RECENT_EXPENSE_AVERAGE,
			coverageMonths = 3,
		)

		val result = policy.assess(
			context = context,
			recommendation = recommendation,
			requestedPrepaymentAmount = 5_000_000L,
			maximumRepaymentAmount = 10_000_000L,
		)

		assertThat(result.cashEquivalentAssetAmount).isEqualTo(8_000_000L)
		assertThat(result.availableRepaymentAmount).isEqualTo(5_000_000L)
		assertThat(result.safeRepaymentLimit).isEqualTo(4_000_000L)
		assertThat(result.appliedPrepaymentAmount).isEqualTo(4_000_000L)
		assertThat(result.remainingCashEquivalentAssetAmount).isEqualTo(4_000_000L)
		assertThat(result.safe).isTrue()
	}

	@Test
	fun `권장 비상자금이 현금성 자산보다 크면 안전 상환 한도는 0이다`() {
		val context = liquidity(liquid = 2_000_000L, emergency = 1_000_000L)
		val recommendation = EmergencyFundRecommendation(
			amount = 5_000_000L,
			basis = EmergencyFundBasis.FALLBACK_POLICY,
			coverageMonths = 3,
		)

		val result = policy.assess(context, recommendation, 1_000_000L, 8_000_000L)

		assertThat(result.safeRepaymentLimit).isZero()
		assertThat(result.appliedPrepaymentAmount).isZero()
		assertThat(result.remainingLiquidAssetAmount).isEqualTo(2_000_000L)
	}

	@Test
	fun `안전 한도보다 대출 잔액이 작으면 대출 잔액까지만 적용한다`() {
		val context = liquidity(liquid = 8_000_000L, emergency = 5_000_000L)
		val recommendation = EmergencyFundRecommendation(
			amount = 4_000_000L,
			basis = EmergencyFundBasis.RECENT_EXPENSE_AVERAGE,
			coverageMonths = 3,
		)

		val result = policy.assess(context, recommendation, 6_000_000L, 2_500_000L)

		assertThat(result.safeRepaymentLimit).isEqualTo(8_000_000L)
		assertThat(result.appliedPrepaymentAmount).isEqualTo(2_500_000L)
	}

	private fun liquidity(
		liquid: Long = 7_000_000L,
		emergency: Long = 1_000_000L,
		averageExpense: Long = 1_000_000L,
		historyMonths: Int = 3,
	) = LiquidityContext(
		baseDate = LocalDate.of(2026, 8, 31),
		liquidAssetAmount = liquid,
		emergencyAssetAmount = emergency,
		averageMonthlyExpenseAmount = averageExpense,
		expenseHistoryMonthCount = historyMonths,
	)
}
