package com.knowave.cashboard.domains.notification.policy

import com.knowave.cashboard.domains.financialschedule.calculator.ScheduleOccurrence
import com.knowave.cashboard.domains.financialschedule.entity.CashFlowDirection
import com.knowave.cashboard.domains.financialschedule.entity.ScheduleType
import com.knowave.cashboard.domains.notification.entity.NotificationType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class PaymentDuePolicyTest {
	private val policy = PaymentDuePolicy()
	private val scheduleId = UUID.fromString("11111111-1111-1111-1111-111111111111")
	private val baseDate = LocalDate.of(2026, 9, 2)
	private val scheduledAt = Instant.parse("2026-09-02T00:00:00Z")

	@ParameterizedTest
	@ValueSource(ints = [0, 1, 3])
	fun `당일 D1 D3 지출 일정은 결제 예정 알림 후보가 된다`(offset: Int) {
		val occurrenceDate = baseDate.plusDays(offset.toLong())

		val candidate = policy.evaluate(baseDate, expenseOccurrence(occurrenceDate), scheduledAt)

		assertThat(candidate).isNotNull
		assertThat(candidate?.type).isEqualTo(NotificationType.PAYMENT_DUE)
		assertThat(candidate?.title).isEqualTo("결제 예정 알림")
		assertThat(candidate?.scheduledAt).isEqualTo(scheduledAt)
		assertThat(candidate?.deduplicationKey).isEqualTo("PAYMENT_DUE:$scheduleId:$occurrenceDate:$offset")
	}

	@Test
	fun `결제일까지 남은 날짜에 따라 알림 문구가 구분된다`() {
		assertThat(policy.evaluate(baseDate, expenseOccurrence(baseDate), scheduledAt)?.message)
			.isEqualTo("오늘 전기요금 12,000원이 결제 예정이에요.")
		assertThat(policy.evaluate(baseDate, expenseOccurrence(baseDate.plusDays(1)), scheduledAt)?.message)
			.isEqualTo("내일 전기요금 12,000원이 결제 예정이에요.")
		assertThat(policy.evaluate(baseDate, expenseOccurrence(baseDate.plusDays(3)), scheduledAt)?.message)
			.isEqualTo("3일 후 전기요금 12,000원이 결제 예정이에요.")
	}

	@Test
	fun `수입 일정과 D2 및 조회 범위 밖 일정은 제외한다`() {
		assertThat(policy.evaluate(baseDate, incomeOccurrence(baseDate.plusDays(1)), scheduledAt)).isNull()
		assertThat(policy.evaluate(baseDate, expenseOccurrence(baseDate.plusDays(2)), scheduledAt)).isNull()
		assertThat(policy.evaluate(baseDate, expenseOccurrence(baseDate.minusDays(1)), scheduledAt)).isNull()
		assertThat(policy.evaluate(baseDate, expenseOccurrence(baseDate.plusDays(4)), scheduledAt)).isNull()
	}

	private fun expenseOccurrence(date: LocalDate) = occurrence(date, CashFlowDirection.EXPENSE)

	private fun incomeOccurrence(date: LocalDate) = occurrence(date, CashFlowDirection.INCOME)

	private fun occurrence(date: LocalDate, direction: CashFlowDirection) = ScheduleOccurrence(
		scheduleId = scheduleId,
		date = date,
		type = ScheduleType.UTILITY,
		title = "전기요금",
		amount = 12_000,
		direction = direction,
	)
}
