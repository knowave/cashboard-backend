package com.knowave.cashboard.domains.financialschedule.service

import com.knowave.cashboard.common.entity.BaseEntity
import com.knowave.cashboard.common.exception.FinancialScheduleDataIntegrityException
import com.knowave.cashboard.common.exception.InvalidEnumValueException
import com.knowave.cashboard.common.exception.InvalidFinancialSchedulePeriodException
import com.knowave.cashboard.domains.financialschedule.calculator.ScheduleOccurrenceGenerator
import com.knowave.cashboard.domains.financialschedule.entity.CashFlowDirection
import com.knowave.cashboard.domains.financialschedule.entity.FinancialSchedule
import com.knowave.cashboard.domains.financialschedule.entity.RecurrenceRule
import com.knowave.cashboard.domains.financialschedule.entity.ScheduleType
import com.knowave.cashboard.domains.financialschedule.repository.FinancialScheduleRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class FinancialScheduleOccurrenceSourceTest {
	private val repository = FakeOccurrenceRepository()
	private val source = FinancialScheduleOccurrenceSource(repository, ScheduleOccurrenceGenerator())

	@Test
	fun `저장 일정을 enum과 반복 규칙으로 변환해 발생일을 반환한다`() {
		repository.candidates = listOf(monthlySchedule(day = 31))

		val result = source.findOccurrences(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28))

		assertThat(repository.lastRange).isEqualTo(
			LocalDate.of(2026, 2, 1) to LocalDate.of(2026, 2, 28),
		)
		assertThat(result.single().date).isEqualTo(LocalDate.of(2026, 2, 28))
	}

	@Test
	fun `저장된 direction enum 손상은 데이터 무결성 예외로 변환한다`() {
		val schedule = monthlySchedule(day = 15).also { it.assignStringField("direction", "BROKEN") }
		repository.candidates = listOf(schedule)

		assertThatThrownBy {
			source.findOccurrences(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28))
		}.isInstanceOf(FinancialScheduleDataIntegrityException::class.java)
			.hasMessageContaining("field=direction")
			.hasCauseInstanceOf(InvalidEnumValueException::class.java)
			.extracting("errorCode", "status")
			.containsExactly("DATA_INTEGRITY_ERROR", HttpStatus.INTERNAL_SERVER_ERROR)
	}

	@Test
	fun `저장된 반복 필드 불일치는 데이터 무결성 예외로 변환한다`() {
		val schedule = monthlySchedule(day = 15).also { it.assignLocalDateField("startDate", null) }
		repository.candidates = listOf(schedule)

		assertThatThrownBy {
			source.findOccurrences(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28))
		}.isInstanceOf(FinancialScheduleDataIntegrityException::class.java)
			.hasMessageContaining("field=recurrence")
			.hasCauseInstanceOf(com.knowave.cashboard.common.exception.InvalidRecurrenceRuleException::class.java)
	}

	@Test
	fun `후보가 없어도 역전된 조회 범위는 거부하고 Repository를 호출하지 않는다`() {
		assertThatThrownBy {
			source.findOccurrences(LocalDate.of(2026, 2, 28), LocalDate.of(2026, 2, 1))
		}.isInstanceOf(InvalidFinancialSchedulePeriodException::class.java)

		assertThat(repository.lastRange).isNull()
	}

	@Test
	fun `서로 다른 저장 일정의 발생일은 날짜 제목 ID 순으로 정렬한다`() {
		repository.candidates = listOf(
			monthlySchedule(15, "나", UUID.fromString("00000000-0000-0000-0000-000000000002")),
			monthlySchedule(15, "가", UUID.fromString("00000000-0000-0000-0000-000000000001")),
		)

		val result = source.findOccurrences(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28))

		assertThat(result.map { it.title }).containsExactly("가", "나")
	}

	private fun monthlySchedule(
		day: Int,
		title: String = "월 반복",
		id: UUID = UUID.randomUUID(),
	) = FinancialSchedule.create(
		type = ScheduleType.ETC,
		title = title,
		amount = 10_000L,
		direction = CashFlowDirection.EXPENSE,
		recurrence = RecurrenceRule.Monthly(day, LocalDate.of(2026, 1, 1), null),
	).also { it.assignBaseFields(id) }
}

private class FakeOccurrenceRepository : FinancialScheduleRepository {
	var candidates: List<FinancialSchedule> = emptyList()
	var lastRange: Pair<LocalDate, LocalDate>? = null

	override fun save(schedule: FinancialSchedule): FinancialSchedule = schedule
	override fun findById(id: UUID): FinancialSchedule? = null
	override fun findAllOrderByCreatedAtDesc(): List<FinancialSchedule> = emptyList()
	override fun findCandidates(from: LocalDate, toInclusive: LocalDate): List<FinancialSchedule> {
		lastRange = from to toInclusive
		return candidates
	}
	override fun delete(schedule: FinancialSchedule) = Unit
}

private fun BaseEntity.assignBaseFields(id: UUID = UUID.randomUUID()) {
	BaseEntity::class.java.getDeclaredField("id").apply {
		isAccessible = true
		set(this@assignBaseFields, id)
	}
	BaseEntity::class.java.getDeclaredField("createdAt").apply {
		isAccessible = true
		set(this@assignBaseFields, LocalDateTime.of(2026, 9, 1, 0, 0))
	}
	BaseEntity::class.java.getDeclaredField("updatedAt").apply {
		isAccessible = true
		set(this@assignBaseFields, LocalDateTime.of(2026, 9, 1, 0, 0))
	}
}

private fun FinancialSchedule.assignStringField(field: String, value: String) {
	FinancialSchedule::class.java.getDeclaredField(field).apply {
		isAccessible = true
		set(this@assignStringField, value)
	}
}

private fun FinancialSchedule.assignLocalDateField(field: String, value: LocalDate?) {
	FinancialSchedule::class.java.getDeclaredField(field).apply {
		isAccessible = true
		set(this@assignLocalDateField, value)
	}
}
