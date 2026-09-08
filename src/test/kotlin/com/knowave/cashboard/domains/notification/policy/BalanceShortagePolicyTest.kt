package com.knowave.cashboard.domains.notification.policy

import com.knowave.cashboard.domains.financialschedule.calculator.CashFlowProjection
import com.knowave.cashboard.domains.financialschedule.calculator.CashFlowSummary
import com.knowave.cashboard.domains.financialschedule.calculator.DailyBalance
import com.knowave.cashboard.domains.notification.entity.NotificationType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate

class BalanceShortagePolicyTest {
	private val policy = BalanceShortagePolicy()

	@Test
	fun `30일 중 최초 음수 예상 잔액일을 선택한다`() {
		val shortage = policy.findFirstShortage(projectionWithBalances(20L, -10L, -30L))

		assertThat(shortage?.date).isEqualTo(LocalDate.of(2026, 9, 25))
		assertThat(shortage?.expectedBalance).isEqualTo(-10L)
	}

	@Test
	fun `예상 잔액이 0원이면 부족으로 판단하지 않는다`() {
		assertThat(policy.findFirstShortage(projectionWithBalances(20L, 0L))).isNull()
	}

	@Test
	fun `부족 후보를 BALANCE_SHORTAGE 알림으로 변환한다`() {
		val notification = policy.toNotification(
			shortage = requireNotNull(policy.findFirstShortage(projectionWithBalances(-10L))),
			episode = 2,
			scheduledAt = Instant.parse("2026-09-02T00:00:00Z"),
		)

		assertThat(notification.type).isEqualTo(NotificationType.BALANCE_SHORTAGE)
		assertThat(notification.deduplicationKey).isEqualTo("BALANCE_SHORTAGE:2:2026-09-24")
		assertThat(notification.message).contains("-10")
	}

	private fun projectionWithBalances(vararg balances: Long): CashFlowProjection {
		val start = LocalDate.of(2026, 9, 24)
		return CashFlowProjection(
			projectionStartDate = start,
			projectionStartBalance = 100L,
			summary = CashFlowSummary(0L, 0L, 0L),
			expectedClosingBalance = balances.lastOrNull() ?: 100L,
			dailyBalances = balances.mapIndexed { index, balance ->
				DailyBalance(start.plusDays(index.toLong()), 0L, 0L, balance)
			},
		)
	}
}
