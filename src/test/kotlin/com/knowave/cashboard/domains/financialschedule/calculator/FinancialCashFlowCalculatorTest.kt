package com.knowave.cashboard.domains.financialschedule.calculator

import com.knowave.cashboard.domains.financialschedule.entity.CashFlowDirection
import com.knowave.cashboard.domains.financialschedule.entity.ScheduleType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

class FinancialCashFlowCalculatorTest {
	private val calculator = FinancialCashFlowCalculator()

	@Test
	fun `수입과 지출을 합산해 순현금흐름을 계산한다`() {
		val result = calculator.summarize(
			listOf(
				occurrence(LocalDate.of(2026, 9, 5), CashFlowDirection.INCOME, 3_100_000L),
				occurrence(LocalDate.of(2026, 9, 7), CashFlowDirection.EXPENSE, 750_000L),
				occurrence(LocalDate.of(2026, 9, 25), CashFlowDirection.EXPENSE, 475_000L),
			),
		)

		assertThat(result.incomeAmount).isEqualTo(3_100_000L)
		assertThat(result.expenseAmount).isEqualTo(1_225_000L)
		assertThat(result.netCashFlow).isEqualTo(1_875_000L)
	}

	@Test
	fun `같은 날짜의 일정을 합산해 하나의 일 마감 잔액을 만든다`() {
		val date = LocalDate.of(2026, 9, 5)
		val result = calculator.project(
			projectionStartDate = date,
			projectionStartBalance = 1_000_000L,
			occurrences = listOf(
				occurrence(date, CashFlowDirection.INCOME, 500_000L),
				occurrence(date, CashFlowDirection.EXPENSE, 200_000L),
			),
		)

		assertThat(result.dailyBalances).containsExactly(
			DailyBalance(date, 500_000L, 200_000L, 1_300_000L),
		)
		assertThat(result.expectedClosingBalance).isEqualTo(1_300_000L)
	}

	@Test
	fun `일별 잔액은 날짜 오름차순으로 누적한다`() {
		val result = calculator.project(
			LocalDate.of(2026, 9, 1),
			1_000L,
			listOf(
				occurrence(LocalDate.of(2026, 9, 25), CashFlowDirection.EXPENSE, 100L),
				occurrence(LocalDate.of(2026, 9, 5), CashFlowDirection.INCOME, 500L),
			),
		)

		assertThat(result.dailyBalances).containsExactly(
			DailyBalance(LocalDate.of(2026, 9, 5), 500L, 0L, 1_500L),
			DailyBalance(LocalDate.of(2026, 9, 25), 0L, 100L, 1_400L),
		)
	}

	@Test
	fun `지출이 시작 잔액보다 크면 음수 마감 잔액을 반환한다`() {
		val result = calculator.project(
			LocalDate.of(2026, 9, 7),
			100_000L,
			listOf(occurrence(LocalDate.of(2026, 9, 7), CashFlowDirection.EXPENSE, 150_000L)),
		)

		assertThat(result.expectedClosingBalance).isEqualTo(-50_000L)
	}

	@Test
	fun `일정이 없으면 시작 잔액을 마감 잔액으로 유지한다`() {
		val result = calculator.project(LocalDate.of(2026, 9, 1), 300_000L, emptyList())

		assertThat(result.summary).isEqualTo(CashFlowSummary(0L, 0L, 0L))
		assertThat(result.dailyBalances).isEmpty()
		assertThat(result.expectedClosingBalance).isEqualTo(300_000L)
	}

	@Test
	fun `마감 잔액 함수는 전체 순현금흐름을 시작 잔액에 적용한다`() {
		val result = calculator.calculateClosingBalance(
			1_000_000L,
			listOf(
				occurrence(LocalDate.of(2026, 9, 5), CashFlowDirection.INCOME, 500_000L),
				occurrence(LocalDate.of(2026, 9, 7), CashFlowDirection.EXPENSE, 200_000L),
			),
		)

		assertThat(result).isEqualTo(1_300_000L)
	}

	@Test
	fun `project는 기준 잔액과 시작일을 변경하지 않는다`() {
		val startDate = LocalDate.of(2026, 9, 1)
		val startBalance = 900_000L

		val result = calculator.project(
			startDate,
			startBalance,
			listOf(occurrence(LocalDate.of(2026, 9, 2), CashFlowDirection.INCOME, 100L)),
		)

		assertThat(result.projectionStartDate).isEqualTo(startDate)
		assertThat(result.projectionStartBalance).isEqualTo(startBalance)
	}

	@Test
	fun `수입 합계가 Long 최대값을 넘으면 오버플로를 거부한다`() {
		assertThatThrownBy {
			calculator.summarize(
				listOf(
					occurrence(LocalDate.of(2026, 9, 1), CashFlowDirection.INCOME, Long.MAX_VALUE),
					occurrence(LocalDate.of(2026, 9, 2), CashFlowDirection.INCOME, 1L),
				),
			)
		}.isInstanceOf(ArithmeticException::class.java)
	}

	@Test
	fun `순현금흐름 차감이 Long 최대값을 넘으면 오버플로를 거부한다`() {
		assertThatThrownBy {
			calculator.summarize(
				listOf(
					occurrence(LocalDate.of(2026, 9, 1), CashFlowDirection.INCOME, Long.MAX_VALUE),
					occurrence(LocalDate.of(2026, 9, 2), CashFlowDirection.EXPENSE, -1L),
				),
			)
		}.isInstanceOf(ArithmeticException::class.java)
	}

	@Test
	fun `기준 잔액 누적이 Long 최대값을 넘으면 오버플로를 거부한다`() {
		assertThatThrownBy {
			calculator.calculateClosingBalance(
				Long.MAX_VALUE,
				listOf(occurrence(LocalDate.of(2026, 9, 1), CashFlowDirection.INCOME, 1L)),
			)
		}.isInstanceOf(ArithmeticException::class.java)
	}

	@Test
	fun `정상적인 음수 잔액도 Long 최소값 아래로 내려가면 오버플로를 거부한다`() {
		assertThatThrownBy {
			calculator.calculateClosingBalance(
				Long.MIN_VALUE,
				listOf(occurrence(LocalDate.of(2026, 9, 1), CashFlowDirection.EXPENSE, 1L)),
			)
		}.isInstanceOf(ArithmeticException::class.java)
	}

	@Test
	fun `다일 누적 중 Long 최대값을 넘으면 오버플로를 거부한다`() {
		assertThatThrownBy {
			calculator.project(
				LocalDate.of(2026, 9, 1),
				Long.MAX_VALUE - 1L,
				listOf(
					occurrence(LocalDate.of(2026, 9, 1), CashFlowDirection.INCOME, 1L),
					occurrence(LocalDate.of(2026, 9, 2), CashFlowDirection.INCOME, 1L),
				),
			)
		}.isInstanceOf(ArithmeticException::class.java)
	}

	private fun occurrence(date: LocalDate, direction: CashFlowDirection, amount: Long) = ScheduleOccurrence(
		scheduleId = UUID.randomUUID(),
		date = date,
		type = ScheduleType.ETC,
		title = "일정",
		amount = amount,
		direction = direction,
	)
}
