package com.knowave.cashboard.domains.financialschedule.calculator

import com.knowave.cashboard.common.exception.InvalidFinancialSchedulePeriodException
import com.knowave.cashboard.domains.financialschedule.entity.CashFlowDirection
import com.knowave.cashboard.domains.financialschedule.entity.RecurrenceRule
import com.knowave.cashboard.domains.financialschedule.entity.ScheduleType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

class ScheduleOccurrenceGeneratorTest {
	private val generator = ScheduleOccurrenceGenerator()

	@Test
	fun `매월 31일은 각 월의 마지막 날로 보정한다`() {
		val definition = definition(
			recurrence = RecurrenceRule.Monthly(
				dayOfMonth = 31,
				startDate = LocalDate.of(2026, 1, 1),
				endDate = null,
			),
		)

		val result = generator.generate(
			definition,
			LocalDate.of(2026, 2, 1),
			LocalDate.of(2026, 4, 30),
		)

		assertThat(result.map { it.date }).containsExactly(
			LocalDate.of(2026, 2, 28),
			LocalDate.of(2026, 3, 31),
			LocalDate.of(2026, 4, 30),
		)
	}

	@Test
	fun `2월 29일 연 반복은 평년 말일과 윤년 29일에 발생한다`() {
		val result = generator.generate(
			definition(recurrence = yearly(month = 2, day = 29, startYear = 2026)),
			LocalDate.of(2027, 1, 1),
			LocalDate.of(2028, 12, 31),
		)

		assertThat(result.map { it.date }).containsExactly(
			LocalDate.of(2027, 2, 28),
			LocalDate.of(2028, 2, 29),
		)
	}

	@Test
	fun `일회성 일정은 조회 범위에 있을 때만 발생한다`() {
		val once = definition(RecurrenceRule.Once(LocalDate.of(2026, 9, 15)))

		assertThat(generator.generate(once, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30)))
			.extracting<LocalDate> { it.date }
			.containsExactly(LocalDate.of(2026, 9, 15))
		assertThat(generator.generate(once, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31)))
			.isEmpty()
	}

	@Test
	fun `반복 시작일 이전 발생은 제외한다`() {
		val result = generator.generate(
			definition(RecurrenceRule.Monthly(15, LocalDate.of(2026, 2, 20), null)),
			LocalDate.of(2026, 1, 1),
			LocalDate.of(2026, 3, 31),
		)

		assertThat(result.map { it.date }).containsExactly(
			LocalDate.of(2026, 3, 15),
		)
	}

	@Test
	fun `종료일은 포함하고 종료일 다음 발생은 제외한다`() {
		val monthly = definition(
			RecurrenceRule.Monthly(15, LocalDate.of(2026, 1, 15), LocalDate.of(2026, 2, 15)),
		)

		assertThat(generator.generate(monthly, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31)).map { it.date })
			.containsExactly(LocalDate.of(2026, 1, 15), LocalDate.of(2026, 2, 15))
	}

	@Test
	fun `역전된 조회 범위는 거부한다`() {
		assertThatThrownBy {
			generator.generate(
				definition(RecurrenceRule.Once(LocalDate.of(2026, 9, 15))),
				LocalDate.of(2026, 9, 30),
				LocalDate.of(2026, 9, 1),
			)
		}.isInstanceOf(InvalidFinancialSchedulePeriodException::class.java)
	}

	@Test
	fun `긴 조회 범위 결과는 날짜 제목 ID 순이며 중복이 없다`() {
		val first = definition(
			recurrence = RecurrenceRule.Monthly(31, LocalDate.of(2020, 1, 1), null),
			title = "가",
			scheduleId = UUID.fromString("00000000-0000-0000-0000-000000000002"),
		)
		val result = generator.generate(first, LocalDate.of(2020, 1, 1), LocalDate.of(2030, 12, 31))
		val sorted = result.sortedWith(compareBy(ScheduleOccurrence::date, ScheduleOccurrence::title, ScheduleOccurrence::scheduleId))

		assertThat(result).hasSize(132)
		assertThat(result.map { it.scheduleId to it.date }.toSet()).hasSize(132)
		assertThat(result).containsExactlyElementsOf(sorted)
	}

	private fun definition(
		recurrence: RecurrenceRule,
		title: String = "일정",
		scheduleId: UUID = UUID.fromString("00000000-0000-0000-0000-000000000001"),
	) = ScheduleDefinition(
		scheduleId = scheduleId,
		type = ScheduleType.ETC,
		title = title,
		amount = 10_000L,
		direction = CashFlowDirection.EXPENSE,
		recurrence = recurrence,
	)

	private fun yearly(month: Int, day: Int, startYear: Int) = RecurrenceRule.Yearly(
		monthOfYear = month,
		dayOfMonth = day,
		startDate = LocalDate.of(startYear, 1, 1),
		endDate = null,
	)
}
