package com.knowave.cashboard.domains.notification.job

import com.knowave.cashboard.domains.financialschedule.calculator.ScheduleOccurrence
import com.knowave.cashboard.domains.financialschedule.context.LiquidityBalanceProvider
import com.knowave.cashboard.domains.financialschedule.entity.CashFlowDirection
import com.knowave.cashboard.domains.financialschedule.entity.ScheduleType
import com.knowave.cashboard.domains.financialschedule.service.CalendarOccurrenceSource
import com.knowave.cashboard.domains.notification.entity.BalanceShortageState
import com.knowave.cashboard.domains.notification.policy.BalanceShortagePolicy
import com.knowave.cashboard.domains.notification.repository.BalanceShortageStateRepository
import com.knowave.cashboard.domains.notification.repository.BalanceShortageTransition
import com.knowave.cashboard.domains.notification.repository.NewNotification
import com.knowave.cashboard.domains.notification.scheduler.NotificationScheduleContext
import com.knowave.cashboard.domains.notification.service.NotificationGenerationService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

class BalanceShortageNotificationJobTest {
	private val occurrenceSource = ShortageOccurrenceSource()
	private val liquidityBalanceProvider = FixedLiquidityBalanceProvider()
	private val stateRepository = InMemoryBalanceShortageStateRepository()
	private val generationService = ShortageGenerationService()
	private val job = BalanceShortageNotificationJob(
		occurrenceSource,
		liquidityBalanceProvider,
		BalanceShortagePolicy(),
		stateRepository,
		generationService,
	)
	private val context = NotificationScheduleContext(
		LocalDate.of(2026, 9, 24),
		Instant.parse("2026-09-24T00:00:00Z"),
		ZoneId.of("Asia/Seoul"),
	)

	@Test
	fun `같은 부족 예상일은 한 번만 생성한다`() {
		occurrenceSource.occurrences = listOf(expense(context.date.plusDays(1), 110L))

		job.run(context)
		job.run(context)

		assertThat(occurrenceSource.requestedRange).isEqualTo(context.date to context.date.plusDays(30))
		assertThat(generationService.created.map(NewNotification::deduplicationKey))
			.containsExactly("BALANCE_SHORTAGE:1:2026-09-25")
	}

	@Test
	fun `발생 건이 없어도 시작 잔액이 음수면 시작일 부족을 생성한다`() {
		liquidityBalanceProvider.balance = -10L

		job.run(context)

		assertThat(generationService.created.map(NewNotification::deduplicationKey))
			.containsExactly("BALANCE_SHORTAGE:1:2026-09-24")
		assertThat(generationService.created.single().message).contains("-10")
	}

	@Test
	fun `미래 회복 수입이 있어도 시작 잔액이 음수면 시작일 부족을 생성한다`() {
		liquidityBalanceProvider.balance = -10L
		occurrenceSource.occurrences = listOf(income(context.date.plusDays(1), 20L))

		job.run(context)

		assertThat(generationService.created.map(NewNotification::deduplicationKey))
			.containsExactly("BALANCE_SHORTAGE:1:2026-09-24")
	}

	@Test
	fun `시작 잔액이 0원이면 부족으로 판단하지 않는다`() {
		liquidityBalanceProvider.balance = 0L

		job.run(context)

		assertThat(generationService.created).isEmpty()
	}

	@Test
	fun `활성 부족 구간의 최초 부족일이 변경되면 새 episode를 생성한다`() {
		occurrenceSource.occurrences = listOf(expense(context.date.plusDays(1), 110L))
		job.run(context)
		occurrenceSource.occurrences = listOf(expense(context.date.plusDays(2), 110L))
		job.run(context)

		assertThat(generationService.created.map(NewNotification::deduplicationKey)).containsExactly(
			"BALANCE_SHORTAGE:1:2026-09-25",
			"BALANCE_SHORTAGE:2:2026-09-26",
		)
	}

	@Test
	fun `정상 회복 후 재부족은 episode를 증가시킨다`() {
		occurrenceSource.occurrences = listOf(expense(context.date.plusDays(1), 110L))
		job.run(context)
		occurrenceSource.occurrences = emptyList()
		job.run(context)
		occurrenceSource.occurrences = listOf(expense(context.date.plusDays(1), 110L))
		job.run(context)

		assertThat(generationService.created.map(NewNotification::deduplicationKey)).containsExactly(
			"BALANCE_SHORTAGE:1:2026-09-25",
			"BALANCE_SHORTAGE:2:2026-09-25",
		)
	}

	private fun expense(date: LocalDate, amount: Long) = ScheduleOccurrence(
		scheduleId = UUID.randomUUID(),
		date = date,
		type = ScheduleType.CARD,
		title = "지출",
		amount = amount,
		direction = CashFlowDirection.EXPENSE,
	)

	private fun income(date: LocalDate, amount: Long) = expense(date, amount).copy(direction = CashFlowDirection.INCOME)
}

private class ShortageOccurrenceSource : CalendarOccurrenceSource {
	var occurrences: List<ScheduleOccurrence> = emptyList()
	var requestedRange: Pair<LocalDate, LocalDate>? = null

	override fun findOccurrences(userId: UUID, from: LocalDate, toInclusive: LocalDate): List<ScheduleOccurrence> {
		requestedRange = from to toInclusive
		return occurrences
	}
}

private class FixedLiquidityBalanceProvider : LiquidityBalanceProvider {
	var balance: Long = 100L

	override fun getCurrentLiquidBalance(userId: UUID): Long = balance
}

private class InMemoryBalanceShortageStateRepository : BalanceShortageStateRepository {
	private var state: BalanceShortageState? = null

	override fun findByUserIdForUpdate(userId: UUID): BalanceShortageState? = state?.takeIf { it.userId == userId }

	override fun save(state: BalanceShortageState): BalanceShortageState {
		this.state = state
		return state
	}

	override fun transition(userId: UUID, shortageDate: LocalDate?): BalanceShortageTransition {
		val current = state ?: BalanceShortageState(userId = userId).also { state = it }
		if (shortageDate == null) {
			current.shortageDate = null
			return BalanceShortageTransition(current.episode, false)
		}
		if (current.shortageDate == shortageDate) return BalanceShortageTransition(current.episode, false)
		current.shortageDate = shortageDate
		current.episode += 1
		return BalanceShortageTransition(current.episode, true)
	}
}

private class ShortageGenerationService : NotificationGenerationService {
	val created = mutableListOf<NewNotification>()

	override fun createIfEnabled(userId: UUID, candidate: NewNotification): Boolean {
		if (created.any { it.deduplicationKey == candidate.deduplicationKey }) return false
		created += candidate
		return true
	}
}
