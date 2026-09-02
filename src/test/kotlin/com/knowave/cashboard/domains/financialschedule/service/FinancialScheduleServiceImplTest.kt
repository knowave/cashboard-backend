package com.knowave.cashboard.domains.financialschedule.service

import com.knowave.cashboard.common.entity.BaseEntity
import com.knowave.cashboard.common.exception.EmptyFinancialSchedulePatchException
import com.knowave.cashboard.common.exception.FinancialScheduleDataIntegrityException
import com.knowave.cashboard.common.exception.InvalidEnumValueException
import com.knowave.cashboard.common.exception.InvalidFinancialScheduleException
import com.knowave.cashboard.common.exception.InvalidRecurrenceRuleException
import com.knowave.cashboard.common.exception.NotFoundException
import com.knowave.cashboard.domains.financialschedule.entity.CashFlowDirection
import com.knowave.cashboard.domains.financialschedule.entity.FinancialSchedule
import com.knowave.cashboard.domains.financialschedule.entity.RecurrenceRule
import com.knowave.cashboard.domains.financialschedule.entity.ScheduleType
import com.knowave.cashboard.domains.financialschedule.repository.FinancialScheduleRepository
import com.knowave.cashboard.domains.financialschedule.service.dto.CreateFinancialScheduleCommand
import com.knowave.cashboard.domains.financialschedule.service.dto.PatchField
import com.knowave.cashboard.domains.financialschedule.service.dto.PatchFinancialScheduleCommand
import com.knowave.cashboard.domains.financialschedule.service.dto.RecurrenceCommand
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class FinancialScheduleServiceImplTest {
	private val repository = FakeFinancialScheduleRepository()
	private val service = FinancialScheduleServiceImpl(repository)

	@Test
	fun `생성 요청의 문자열 enum을 정규화해 저장한다`() {
		val result = service.create(
			CreateFinancialScheduleCommand(
				type = "loan",
				title = "대출 상환",
				amount = 475_000L,
				direction = "expense",
				recurrence = RecurrenceCommand(
					type = "monthly",
					dayOfMonth = 25,
					startDate = LocalDate.of(2026, 1, 1),
				),
			),
		)

		assertThat(result.type).isEqualTo("LOAN")
		assertThat(result.direction).isEqualTo("EXPENSE")
		assertThat(result.recurrence.type).isEqualTo("MONTHLY")
	}

	@Test
	fun `PATCH에서 생략한 값은 유지하고 전달한 값만 변경한다`() {
		val saved = repository.save(monthlySchedule())

		val result = service.patch(
			requireNotNull(saved.id),
			PatchFinancialScheduleCommand(
				type = PatchField.Absent,
				title = PatchField.Present("변경된 제목"),
				amount = PatchField.Present(500_000L),
				direction = PatchField.Absent,
				recurrence = PatchField.Absent,
			),
		)

		assertThat(result.title).isEqualTo("변경된 제목")
		assertThat(result.amount).isEqualTo(500_000L)
		assertThat(result.recurrence.type).isEqualTo("MONTHLY")
	}

	@Test
	fun `유효하지 않은 생성 enum은 사용자 입력 오류로 처리한다`() {
		assertThatThrownBy { service.create(command(type = "UNKNOWN")) }
			.isInstanceOf(InvalidEnumValueException::class.java)
	}

	@Test
	fun `빈 PATCH는 거부한다`() {
		assertThatThrownBy { service.patch(UUID.randomUUID(), emptyPatch()) }
			.isInstanceOf(EmptyFinancialSchedulePatchException::class.java)
	}

	@Test
	fun `PATCH의 null 필드는 거부한다`() {
		assertThatThrownBy { service.patch(UUID.randomUUID(), patch(title = PatchField.Present(null))) }
			.isInstanceOf(InvalidFinancialScheduleException::class.java)
	}

	@Test
	fun `없는 일정 조회는 NotFoundException으로 처리한다`() {
		assertThatThrownBy { service.get(UUID.randomUUID()) }
			.isInstanceOf(NotFoundException::class.java)
	}

	@Test
	fun `반복 유형별 필수 필드와 금지 필드 조합을 검증한다`() {
		assertThatThrownBy {
			service.create(command(recurrence = RecurrenceCommand(type = "ONCE")))
		}.isInstanceOf(InvalidRecurrenceRuleException::class.java)
		assertThatThrownBy {
			service.create(command(recurrence = RecurrenceCommand(
				type = "ONCE",
				scheduledDate = LocalDate.of(2026, 9, 1),
				dayOfMonth = 1,
			)))
		}.isInstanceOf(InvalidRecurrenceRuleException::class.java)
		assertThatThrownBy {
			service.create(command(recurrence = RecurrenceCommand(type = "MONTHLY", startDate = LocalDate.of(2026, 1, 1))))
		}.isInstanceOf(InvalidRecurrenceRuleException::class.java)
		assertThatThrownBy {
			service.create(command(recurrence = RecurrenceCommand(
				type = "MONTHLY",
				scheduledDate = LocalDate.of(2026, 9, 1),
				dayOfMonth = 1,
				startDate = LocalDate.of(2026, 1, 1),
			)))
		}.isInstanceOf(InvalidRecurrenceRuleException::class.java)
		assertThatThrownBy {
			service.create(command(recurrence = RecurrenceCommand(
				type = "YEARLY",
				dayOfMonth = 1,
				startDate = LocalDate.of(2026, 1, 1),
			)))
		}.isInstanceOf(InvalidRecurrenceRuleException::class.java)
		assertThatThrownBy {
			service.create(command(recurrence = RecurrenceCommand(
				type = "YEARLY",
				scheduledDate = LocalDate.of(2026, 9, 1),
				monthOfYear = 1,
				dayOfMonth = 1,
				startDate = LocalDate.of(2026, 1, 1),
			)))
		}.isInstanceOf(InvalidRecurrenceRuleException::class.java)
	}

	@Test
	fun `PATCH recurrence은 기존 반복 필드를 원자적으로 전체 교체한다`() {
		val saved = repository.save(monthlySchedule())

		val result = service.patch(
			requireNotNull(saved.id),
			PatchFinancialScheduleCommand(recurrence = PatchField.Present(RecurrenceCommand(
				type = "ONCE",
				scheduledDate = LocalDate.of(2026, 9, 15),
			))),
		)

		assertThat(result.recurrence).isEqualTo(
			com.knowave.cashboard.domains.financialschedule.service.dto.RecurrenceResult(
				type = "ONCE",
				scheduledDate = LocalDate.of(2026, 9, 15),
			),
		)
	}

	@Test
	fun `없는 일정의 PATCH와 삭제는 NotFoundException으로 처리한다`() {
		val unknownId = UUID.randomUUID()

		assertThatThrownBy { service.patch(unknownId, patch(title = PatchField.Present("변경"))) }
			.isInstanceOf(NotFoundException::class.java)
		assertThatThrownBy { service.delete(unknownId) }
			.isInstanceOf(NotFoundException::class.java)
	}

	@Test
	fun `삭제한 일정은 다시 조회할 수 없다`() {
		val saved = repository.save(monthlySchedule())

		service.delete(requireNotNull(saved.id))

		assertThatThrownBy { service.get(requireNotNull(saved.id)) }
			.isInstanceOf(NotFoundException::class.java)
	}

	@Test
	fun `전체 조회는 생성 시각 내림차순과 ID 오름차순으로 정렬한다`() {
		val firstId = UUID.fromString("00000000-0000-0000-0000-000000000001")
		val secondId = UUID.fromString("00000000-0000-0000-0000-000000000002")
		val latestId = UUID.fromString("00000000-0000-0000-0000-000000000003")
		repository.save(monthlySchedule().apply { assignBaseFields(firstId, LocalDateTime.of(2026, 9, 1, 0, 0)) })
		repository.save(monthlySchedule().apply { assignBaseFields(secondId, LocalDateTime.of(2026, 9, 1, 0, 0)) })
		repository.save(monthlySchedule().apply { assignBaseFields(latestId, LocalDateTime.of(2026, 9, 2, 0, 0)) })

		assertThat(service.getAll().map { it.id }).containsExactly(latestId, firstId, secondId)
	}

	@Test
	fun `저장된 enum 손상은 원인 예외를 보존한 서버 오류로 변환한다`() {
		val saved = repository.save(monthlySchedule())
		saved.assignStringField("scheduleType", "BROKEN")

		assertThatThrownBy { service.get(requireNotNull(saved.id)) }
			.isInstanceOf(FinancialScheduleDataIntegrityException::class.java)
			.hasCauseInstanceOf(InvalidEnumValueException::class.java)
			.extracting("status")
			.isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
	}

	private fun command(
		type: String = "LOAN",
		recurrence: RecurrenceCommand = RecurrenceCommand(
			type = "MONTHLY",
			dayOfMonth = 25,
			startDate = LocalDate.of(2026, 1, 1),
		),
	) = CreateFinancialScheduleCommand(
		type = type,
		title = "대출 상환",
		amount = 475_000L,
		direction = "EXPENSE",
		recurrence = recurrence,
	)

	private fun emptyPatch() = PatchFinancialScheduleCommand()

	private fun patch(title: PatchField<String>) = PatchFinancialScheduleCommand(title = title)

	private fun monthlySchedule() = FinancialSchedule.create(
		type = ScheduleType.LOAN,
		title = "대출 상환",
		amount = 475_000L,
		direction = CashFlowDirection.EXPENSE,
		recurrence = RecurrenceRule.Monthly(25, LocalDate.of(2026, 1, 1), null),
	)
}

private class FakeFinancialScheduleRepository : FinancialScheduleRepository {
	private val schedules = linkedMapOf<UUID, FinancialSchedule>()

	override fun save(schedule: FinancialSchedule): FinancialSchedule {
		if (schedule.id == null) {
			schedule.assignBaseFields()
		}
		schedules[requireNotNull(schedule.id)] = schedule
		return schedule
	}

	override fun findById(id: UUID): FinancialSchedule? = schedules[id]

	override fun findAllOrderByCreatedAtDesc(): List<FinancialSchedule> =
		schedules.values.sortedWith(compareByDescending<FinancialSchedule> { it.createdAt }.thenBy { it.id })

	override fun findCandidates(from: LocalDate, toInclusive: LocalDate): List<FinancialSchedule> = emptyList()

	override fun delete(schedule: FinancialSchedule) {
		schedules.remove(schedule.id)
	}
}

private fun BaseEntity.assignBaseFields(
	id: UUID = UUID.randomUUID(),
	createdAt: LocalDateTime = LocalDateTime.of(2026, 9, 1, 0, 0),
) {
	BaseEntity::class.java.getDeclaredField("id").apply {
		isAccessible = true
		set(this@assignBaseFields, id)
	}
	BaseEntity::class.java.getDeclaredField("createdAt").apply {
		isAccessible = true
		set(this@assignBaseFields, createdAt)
	}
	BaseEntity::class.java.getDeclaredField("updatedAt").apply {
		isAccessible = true
		set(this@assignBaseFields, createdAt)
	}
}

private fun FinancialSchedule.assignStringField(field: String, value: String) {
	FinancialSchedule::class.java.getDeclaredField(field).apply {
		isAccessible = true
		set(this@assignStringField, value)
	}
}
