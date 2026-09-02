package com.knowave.cashboard.domains.financialschedule.service

import com.knowave.cashboard.common.exception.ProjectionRangeExceededException
import com.knowave.cashboard.domains.financialschedule.calculator.CashFlowSummary
import com.knowave.cashboard.domains.financialschedule.calculator.ScheduleOccurrence
import com.knowave.cashboard.domains.financialschedule.context.LiquidityBalanceProvider
import com.knowave.cashboard.domains.financialschedule.entity.CashFlowDirection
import com.knowave.cashboard.domains.financialschedule.entity.ScheduleType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.DateTimeException
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import java.util.UUID

class FinancialCalendarServiceImplTest {
	private val source = FakeCalendarOccurrenceSource()
	private val liquidity = FakeLiquidityBalanceProvider()
	private val clock = Clock.fixed(Instant.parse("2026-09-15T00:00:00Z"), ZoneOffset.UTC)
	private val service = FinancialCalendarServiceImpl(
		occurrenceSource = source,
		liquidityBalanceProvider = liquidity,
		clock = clock,
	)

	@Test
	fun `과거 월은 전체 합계만 제공하고 유동 잔액을 읽지 않는다`() {
		source.defaultOccurrences = listOf(expense(LocalDate.of(2026, 8, 7), 750_000L))

		val result = service.getCalendar(2026, 8)

		assertThat(result.summary).isEqualTo(CashFlowSummary(0L, 750_000L, -750_000L))
		assertThat(result.projection).isNull()
		assertThat(liquidity.callCount).isZero()
	}

	@Test
	fun `현재 월은 기준일보다 늦은 일정만 현재 잔액에 반영한다`() {
		liquidity.balance = 1_200_000L
		source.defaultOccurrences = listOf(
			income(LocalDate.of(2026, 9, 5), 3_100_000L),
			expense(LocalDate.of(2026, 9, 15), 520_000L),
			expense(LocalDate.of(2026, 9, 25), 475_000L),
		)

		val result = service.getCalendar(2026, 9)

		assertThat(result.summary).isEqualTo(CashFlowSummary(3_100_000L, 995_000L, 2_105_000L))
		assertThat(result.projection!!.projectionStartDate).isEqualTo(LocalDate.of(2026, 9, 16))
		assertThat(result.projection!!.projectionStartBalance).isEqualTo(1_200_000L)
		assertThat(result.projection!!.projectedIncomeAmount).isZero()
		assertThat(result.projection!!.projectedExpenseAmount).isEqualTo(475_000L)
		assertThat(result.projection!!.expectedClosingBalance).isEqualTo(725_000L)
	}

	@Test
	fun `현재 월 마지막 날은 다음 날부터 시작하는 빈 projection을 제공한다`() {
		val lastDayService = FinancialCalendarServiceImpl(
			occurrenceSource = source,
			liquidityBalanceProvider = liquidity,
			clock = Clock.fixed(Instant.parse("2026-09-30T00:00:00Z"), ZoneOffset.UTC),
		)
		liquidity.balance = 1_200_000L
		source.defaultOccurrences = listOf(expense(LocalDate.of(2026, 9, 30), 500_000L))

		val result = lastDayService.getCalendar(2026, 9)

		assertThat(result.projection!!.projectionStartDate).isEqualTo(LocalDate.of(2026, 10, 1))
		assertThat(result.projection!!.dailyBalances).isEmpty()
		assertThat(result.projection!!.expectedClosingBalance).isEqualTo(1_200_000L)
	}

	@Test
	fun `미래 월 시작 잔액은 현재 이후부터 조회 월 직전까지 누적한다`() {
		liquidity.balance = 1_200_000L
		source.resultsByRange[LocalDate.of(2026, 9, 16) to LocalDate.of(2026, 9, 30)] =
			listOf(expense(LocalDate.of(2026, 9, 25), 475_000L))
		source.resultsByRange[LocalDate.of(2026, 10, 1) to LocalDate.of(2026, 10, 31)] =
			listOf(income(LocalDate.of(2026, 10, 5), 3_100_000L))

		val result = service.getCalendar(2026, 10)

		assertThat(result.projection!!.projectionStartBalance).isEqualTo(725_000L)
		assertThat(result.projection!!.expectedClosingBalance).isEqualTo(3_825_000L)
	}

	@Test
	fun `현재 월 마지막 날의 다음 달은 빈 bridge 구간을 조회하지 않는다`() {
		val lastDayService = FinancialCalendarServiceImpl(
			occurrenceSource = source,
			liquidityBalanceProvider = liquidity,
			clock = Clock.fixed(Instant.parse("2026-09-30T00:00:00Z"), ZoneOffset.UTC),
		)
		liquidity.balance = 1_200_000L

		val result = lastDayService.getCalendar(2026, 10)

		assertThat(result.projection!!.projectionStartBalance).isEqualTo(1_200_000L)
		assertThat(source.calls).containsExactly(LocalDate.of(2026, 10, 1) to LocalDate.of(2026, 10, 31))
	}

	@Test
	fun `일정이 없는 미래 월은 시작 잔액을 마감 잔액으로 유지한다`() {
		liquidity.balance = 1_200_000L

		val result = service.getCalendar(2026, 10)

		assertThat(result.items).isEmpty()
		assertThat(result.summary).isEqualTo(CashFlowSummary(0L, 0L, 0L))
		assertThat(result.projection!!.expectedClosingBalance).isEqualTo(1_200_000L)
	}

	@Test
	fun `정확히 600개월 미래 조회는 허용하고 601개월은 거부한다`() {
		val current = YearMonth.of(2026, 9)
		val allowed = current.plusMonths(600)
		val rejected = current.plusMonths(601)

		assertThatCode { service.getCalendar(allowed.year, allowed.monthValue) }.doesNotThrowAnyException()
		assertThatThrownBy { service.getCalendar(rejected.year, rejected.monthValue) }
			.isInstanceOf(ProjectionRangeExceededException::class.java)
			.hasMessageContaining("months=601")
	}

	@Test
	fun `유효하지 않은 year 또는 month는 날짜 예외를 전파한다`() {
		assertThatThrownBy { service.getCalendar(1_000_000_000, 1) }
			.isInstanceOf(DateTimeException::class.java)
		assertThatThrownBy { service.getCalendar(2026, 0) }
			.isInstanceOf(DateTimeException::class.java)
	}

	@Test
	fun `projection 중 발생한 정확 산술 예외를 전파한다`() {
		liquidity.balance = Long.MAX_VALUE
		source.defaultOccurrences = listOf(income(LocalDate.of(2026, 9, 16), 1L))

		assertThatThrownBy { service.getCalendar(2026, 9) }
			.isInstanceOf(ArithmeticException::class.java)
	}

	private fun income(date: LocalDate, amount: Long) = occurrence(date, CashFlowDirection.INCOME, amount)
	private fun expense(date: LocalDate, amount: Long) = occurrence(date, CashFlowDirection.EXPENSE, amount)

	private fun occurrence(date: LocalDate, direction: CashFlowDirection, amount: Long) = ScheduleOccurrence(
		scheduleId = UUID.randomUUID(),
		date = date,
		type = ScheduleType.ETC,
		title = "일정",
		amount = amount,
		direction = direction,
	)
}

private class FakeCalendarOccurrenceSource : CalendarOccurrenceSource {
	var defaultOccurrences: List<ScheduleOccurrence> = emptyList()
	val resultsByRange = mutableMapOf<Pair<LocalDate, LocalDate>, List<ScheduleOccurrence>>()
	val calls = mutableListOf<Pair<LocalDate, LocalDate>>()

	override fun findOccurrences(from: LocalDate, toInclusive: LocalDate): List<ScheduleOccurrence> {
		calls += from to toInclusive
		return resultsByRange[from to toInclusive] ?: defaultOccurrences
	}
}

private class FakeLiquidityBalanceProvider : LiquidityBalanceProvider {
	var balance: Long = 0L
	var callCount: Int = 0

	override fun getCurrentLiquidBalance(): Long {
		callCount += 1
		return balance
	}
}
