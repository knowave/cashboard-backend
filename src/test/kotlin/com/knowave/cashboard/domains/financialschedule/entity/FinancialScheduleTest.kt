package com.knowave.cashboard.domains.financialschedule.entity

import com.knowave.cashboard.common.exception.InvalidFinancialScheduleException
import com.knowave.cashboard.common.exception.InvalidFinancialSchedulePeriodException
import com.knowave.cashboard.common.exception.InvalidEnumValueException
import com.knowave.cashboard.common.exception.InvalidRecurrenceRuleException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.LocalDate

class FinancialScheduleTest {
	@Test
	fun `월 반복 일정을 생성하면 enum name과 반복 필드만 저장한다`() {
		val schedule = FinancialSchedule.create(
			type = ScheduleType.LOAN,
			title = "  신용대출 상환  ",
			amount = 475_000L,
			direction = CashFlowDirection.EXPENSE,
			recurrence = RecurrenceRule.Monthly(
				dayOfMonth = 25,
				startDate = LocalDate.of(2026, 1, 1),
				endDate = null,
			),
		)

		assertThat(schedule.scheduleType).isEqualTo("LOAN")
		assertThat(schedule.title).isEqualTo("신용대출 상환")
		assertThat(schedule.direction).isEqualTo("EXPENSE")
		assertThat(schedule.recurrenceType).isEqualTo("MONTHLY")
		assertThat(schedule.dayOfMonth).isEqualTo(25)
		assertThat(schedule.scheduledDate).isNull()
		assertThat(schedule.monthOfYear).isNull()
	}

	@Test
	fun `반복 규칙을 ONCE로 바꾸면 이전 반복 필드를 제거한다`() {
		val schedule = monthlySchedule()

		schedule.update(
			type = null,
			title = null,
			amount = null,
			direction = null,
			recurrence = RecurrenceRule.Once(LocalDate.of(2026, 9, 15)),
		)

		assertThat(schedule.recurrenceType).isEqualTo("ONCE")
		assertThat(schedule.scheduledDate).isEqualTo(LocalDate.of(2026, 9, 15))
		assertThat(schedule.dayOfMonth).isNull()
		assertThat(schedule.startDate).isNull()
		assertThat(schedule.endDate).isNull()
	}

	@Test
	fun `연 반복 규칙을 ONCE로 바꾸면 monthOfYear를 포함한 이전 반복 필드를 제거한다`() {
		val schedule = FinancialSchedule.create(
			type = ScheduleType.INSURANCE,
			title = "보험료",
			amount = 120_000L,
			direction = CashFlowDirection.EXPENSE,
			recurrence = yearlyRule(month = 6, day = 15),
		)

		schedule.update(
			type = null,
			title = null,
			amount = null,
			direction = null,
			recurrence = RecurrenceRule.Once(LocalDate.of(2026, 9, 15)),
		)

		assertThat(schedule.recurrenceType).isEqualTo("ONCE")
		assertThat(schedule.scheduledDate).isEqualTo(LocalDate.of(2026, 9, 15))
		assertThat(schedule.monthOfYear).isNull()
		assertThat(schedule.dayOfMonth).isNull()
		assertThat(schedule.startDate).isNull()
		assertThat(schedule.endDate).isNull()
	}

	@Test
	fun `일회성 반복 규칙을 월 반복 규칙으로 바꾸면 scheduledDate를 제거한다`() {
		val schedule = FinancialSchedule.create(
			type = ScheduleType.SALARY,
			title = "급여",
			amount = 3_000_000L,
			direction = CashFlowDirection.INCOME,
			recurrence = RecurrenceRule.Once(LocalDate.of(2026, 9, 15)),
		)

		schedule.update(
			type = null,
			title = null,
			amount = null,
			direction = null,
			recurrence = monthlyRule(),
		)

		assertThat(schedule.recurrenceType).isEqualTo("MONTHLY")
		assertThat(schedule.scheduledDate).isNull()
		assertThat(schedule.monthOfYear).isNull()
		assertThat(schedule.dayOfMonth).isEqualTo(25)
	}

	@Test
	fun `월 반복의 일자는 1부터 31 사이여야 한다`() {
		assertThatThrownBy { monthlyRule(dayOfMonth = 0) }
			.isInstanceOf(InvalidRecurrenceRuleException::class.java)
	}

	@Test
	fun `연 반복의 월과 일자는 유효한 조합이어야 한다`() {
		assertThatThrownBy { yearlyRule(month = 4, day = 31) }
			.isInstanceOf(InvalidRecurrenceRuleException::class.java)
	}

	@Test
	fun `반복 종료일은 시작일보다 빠를 수 없다`() {
		assertThatThrownBy {
			monthlyRule(
				start = LocalDate.of(2026, 2, 1),
				end = LocalDate.of(2026, 1, 31),
			)
		}.isInstanceOf(InvalidFinancialSchedulePeriodException::class.java)
	}

	@Test
	fun `일정 금액은 0보다 커야 한다`() {
		assertThatThrownBy { schedule(amount = 0L) }
			.isInstanceOf(InvalidFinancialScheduleException::class.java)
	}

	@Test
	fun `수정할 제목은 앞뒤 공백을 제거해 저장한다`() {
		val schedule = monthlySchedule()

		schedule.update(
			type = null,
			title = "  월세  ",
			amount = null,
			direction = null,
			recurrence = null,
		)

		assertThat(schedule.title).isEqualTo("월세")
	}

	@Test
	fun `수정할 제목은 공백만으로 구성될 수 없다`() {
		assertThatThrownBy {
			monthlySchedule().update(null, "   ", null, null, null)
		}.isInstanceOf(InvalidFinancialScheduleException::class.java)
	}

	@Test
	fun `수정할 제목은 100자를 초과할 수 없다`() {
		assertThatThrownBy {
			monthlySchedule().update(null, "a".repeat(101), null, null, null)
		}.isInstanceOf(InvalidFinancialScheduleException::class.java)
	}

	@Test
	fun `생성할 제목은 앞뒤 공백을 제거한 뒤 100자를 초과할 수 없다`() {
		assertThatThrownBy {
			FinancialSchedule.create(
				type = ScheduleType.LOAN,
				title = " ${"a".repeat(101)} ",
				amount = 475_000L,
				direction = CashFlowDirection.EXPENSE,
				recurrence = monthlyRule(),
			)
		}.isInstanceOf(InvalidFinancialScheduleException::class.java)
	}

	@Test
	fun `수정할 금액은 0보다 커야 한다`() {
		assertThatThrownBy {
			monthlySchedule().update(null, null, 0L, null, null)
		}.isInstanceOf(InvalidFinancialScheduleException::class.java)
		assertThatThrownBy {
			monthlySchedule().update(null, null, -1L, null, null)
		}.isInstanceOf(InvalidFinancialScheduleException::class.java)
	}

	@Test
	fun `enum from은 공백과 소문자를 정규화한다`() {
		assertThat(ScheduleType.from("  loan ")).isEqualTo(ScheduleType.LOAN)
		assertThat(CashFlowDirection.from(" expense ")).isEqualTo(CashFlowDirection.EXPENSE)
		assertThat(RecurrenceType.from(" yearly ")).isEqualTo(RecurrenceType.YEARLY)
	}

	@Test
	fun `enum from은 잘못된 값을 거부한다`() {
		assertThatThrownBy { ScheduleType.from("invalid") }
			.isInstanceOf(InvalidEnumValueException::class.java)
		assertThatThrownBy { CashFlowDirection.from("invalid") }
			.isInstanceOf(InvalidEnumValueException::class.java)
		assertThatThrownBy { RecurrenceType.from("invalid") }
			.isInstanceOf(InvalidEnumValueException::class.java)
	}

	private fun monthlyRule(
		dayOfMonth: Int = 25,
		start: LocalDate = LocalDate.of(2026, 1, 1),
		end: LocalDate? = null,
	) = RecurrenceRule.Monthly(dayOfMonth, start, end)

	private fun yearlyRule(month: Int, day: Int) = RecurrenceRule.Yearly(
		monthOfYear = month,
		dayOfMonth = day,
		startDate = LocalDate.of(2026, 1, 1),
		endDate = null,
	)

	private fun schedule(amount: Long = 475_000L) = FinancialSchedule.create(
		type = ScheduleType.LOAN,
		title = "대출 상환",
		amount = amount,
		direction = CashFlowDirection.EXPENSE,
		recurrence = monthlyRule(),
	)

	private fun monthlySchedule() = schedule()
}
