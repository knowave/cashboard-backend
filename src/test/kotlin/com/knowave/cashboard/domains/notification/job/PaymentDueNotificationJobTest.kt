package com.knowave.cashboard.domains.notification.job

import com.knowave.cashboard.domains.financialschedule.calculator.ScheduleOccurrence
import com.knowave.cashboard.domains.financialschedule.entity.CashFlowDirection
import com.knowave.cashboard.domains.financialschedule.entity.ScheduleType
import com.knowave.cashboard.domains.financialschedule.service.CalendarOccurrenceSource
import com.knowave.cashboard.domains.notification.repository.NewNotification
import com.knowave.cashboard.domains.notification.policy.PaymentDuePolicy
import com.knowave.cashboard.domains.notification.scheduler.NotificationScheduleContext
import com.knowave.cashboard.domains.notification.service.NotificationGenerationService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

class PaymentDueNotificationJobTest {
	private val occurrenceSource = FakeCalendarOccurrenceSource()
	private val generationService = IdempotentGenerationService()
	private val job = PaymentDueNotificationJob(occurrenceSource, PaymentDuePolicy(), generationService)
	private val baseDate = LocalDate.of(2026, 9, 2)
	private val context = NotificationScheduleContext(baseDate, Instant.parse("2026-09-02T00:00:00Z"), ZoneId.of("Asia/Seoul"))

	@Test
	fun `D3 D1 당일 지출 발생 건만 생성 서비스에 전달한다`() {
		val d3Id = UUID.fromString("11111111-1111-1111-1111-111111111111")
		val d1Id = UUID.fromString("22222222-2222-2222-2222-222222222222")
		val todayId = UUID.fromString("33333333-3333-3333-3333-333333333333")
		occurrenceSource.occurrences = listOf(
			occurrence(d3Id, baseDate.plusDays(3), CashFlowDirection.EXPENSE),
			occurrence(d1Id, baseDate.plusDays(1), CashFlowDirection.EXPENSE),
			occurrence(todayId, baseDate, CashFlowDirection.EXPENSE),
			occurrence(UUID.randomUUID(), baseDate.plusDays(2), CashFlowDirection.EXPENSE),
			occurrence(UUID.randomUUID(), baseDate.plusDays(1), CashFlowDirection.INCOME),
		)

		job.run(context)

		assertThat(occurrenceSource.requestedRange).isEqualTo(baseDate to baseDate.plusDays(3))
		assertThat(generationService.created.map(NewNotification::deduplicationKey)).containsExactlyInAnyOrder(
			"PAYMENT_DUE:$d3Id:${baseDate.plusDays(3)}:3",
			"PAYMENT_DUE:$d1Id:${baseDate.plusDays(1)}:1",
			"PAYMENT_DUE:$todayId:$baseDate:0",
		)
	}

	@Test
	fun `반복 발생 건으로 Job을 다시 실행해도 발생일과 offset별 알림은 한 번만 저장된다`() {
		val scheduleId = UUID.fromString("11111111-1111-1111-1111-111111111111")
		occurrenceSource.occurrences = listOf(
			occurrence(scheduleId, baseDate.plusDays(1), CashFlowDirection.EXPENSE),
			occurrence(scheduleId, baseDate.plusDays(3), CashFlowDirection.EXPENSE),
		)

		job.run(context)
		job.run(context)

		assertThat(generationService.created).hasSize(2)
		assertThat(generationService.created.map(NewNotification::deduplicationKey)).containsExactlyInAnyOrder(
			"PAYMENT_DUE:$scheduleId:${baseDate.plusDays(1)}:1",
			"PAYMENT_DUE:$scheduleId:${baseDate.plusDays(3)}:3",
		)
	}

	@Test
	fun `Job은 다른 예정 알림 정책과 분리된 새 트랜잭션으로 실행된다`() {
		val transaction = requireNotNull(PaymentDueNotificationJob::class.java
			.getDeclaredMethod("run", NotificationScheduleContext::class.java)
			.getAnnotation(Transactional::class.java))

		assertThat(transaction.propagation).isEqualTo(Propagation.REQUIRES_NEW)
	}

	private fun occurrence(scheduleId: UUID, date: LocalDate, direction: CashFlowDirection) = ScheduleOccurrence(
		scheduleId = scheduleId,
		date = date,
		type = ScheduleType.CARD,
		title = "카드대금",
		amount = 52_000,
		direction = direction,
	)
}

private class FakeCalendarOccurrenceSource : CalendarOccurrenceSource {
	var occurrences: List<ScheduleOccurrence> = emptyList()
	var requestedRange: Pair<LocalDate, LocalDate>? = null

	override fun findOccurrences(userId: UUID, from: LocalDate, toInclusive: LocalDate): List<ScheduleOccurrence> {
		requestedRange = from to toInclusive
		return occurrences
	}
}

private class IdempotentGenerationService : NotificationGenerationService {
	val created = mutableListOf<NewNotification>()

	override fun createIfEnabled(userId: UUID, candidate: NewNotification): Boolean {
		if (created.any { it.deduplicationKey == candidate.deduplicationKey }) return false
		created += candidate
		return true
	}
}
