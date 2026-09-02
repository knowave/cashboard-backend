package com.knowave.cashboard.domains.financialschedule.controller

import com.knowave.cashboard.common.exception.InvalidFinancialScheduleException
import com.knowave.cashboard.common.exception.NotFoundException
import com.knowave.cashboard.domains.financialschedule.service.FinancialScheduleService
import com.knowave.cashboard.domains.financialschedule.service.dto.CreateFinancialScheduleCommand
import com.knowave.cashboard.domains.financialschedule.service.dto.FinancialScheduleResult
import com.knowave.cashboard.domains.financialschedule.service.dto.PatchField
import com.knowave.cashboard.domains.financialschedule.service.dto.PatchFinancialScheduleCommand
import com.knowave.cashboard.domains.financialschedule.service.dto.RecurrenceCommand
import com.knowave.cashboard.domains.financialschedule.service.dto.RecurrenceResult
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@WebMvcTest(FinancialScheduleController::class)
class FinancialScheduleControllerTest {

	@Autowired
	private lateinit var mockMvc: MockMvc

	@MockitoBean
	private lateinit var financialScheduleService: FinancialScheduleService

	private val scheduleId = UUID.fromString("3de248b5-f018-4295-991f-43e9804bb7fb")

	@Test
	fun `생성 요청은 201과 월 반복 일정을 반환한다`() {
		given(financialScheduleService.create(anyValue(createCommandFallback()))).willReturn(monthlyResult())

		mockMvc.perform(
			post("/financial-schedules")
				.contentType(MediaType.APPLICATION_JSON)
				.content(
					"""
					{
					  "type": "LOAN",
					  "title": "신용대출 상환",
					  "amount": 475000,
					  "direction": "EXPENSE",
					  "recurrence": {
					    "type": "MONTHLY",
					    "dayOfMonth": 25,
					    "startDate": "2026-01-01",
					    "endDate": null
					  }
					}
					""".trimIndent(),
				),
		)
			.andExpect(status().isCreated)
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.recurrence.type").value("MONTHLY"))
			.andExpect(jsonPath("$.data.recurrence.scheduledDate").doesNotExist())
			.andExpect(jsonPath("$.data.recurrence.monthOfYear").doesNotExist())
			.andExpect(jsonPath("$.data.recurrence.dayOfMonth").value(25))
			.andExpect(jsonPath("$.data.recurrence.startDate").value("2026-01-01"))
			.andExpect(jsonPath("$.data.recurrence.endDate").doesNotExist())
	}

	@Test
	fun `공백을 제거하면 정확히 100자인 생성 제목은 Service까지 전달되어 성공한다`() {
		val title = "a".repeat(100)
		given(financialScheduleService.create(anyValue(createCommandFallback()))).willReturn(monthlyResult(title = title))

		mockMvc.perform(
			post("/financial-schedules")
				.contentType(MediaType.APPLICATION_JSON)
				.content(
					"""
					{
					  "type": "LOAN",
					  "title": " $title ",
					  "amount": 475000,
					  "direction": "EXPENSE",
					  "recurrence": { "type": "ONCE", "scheduledDate": "2026-01-01" }
					}
					""".trimIndent(),
				),
		)
			.andExpect(status().isCreated)

		val captor = ArgumentCaptor.forClass(CreateFinancialScheduleCommand::class.java)
		then(financialScheduleService).should().create(captureValue(captor, createCommandFallback()))
		check(captor.value.title == " $title ")
	}

	@Test
	fun `공백을 제거해도 101자인 생성 제목은 400을 반환한다`() {
		val title = "a".repeat(101)
		given(financialScheduleService.create(anyValue(createCommandFallback()))).willThrow(
			InvalidFinancialScheduleException("title must contain between 1 and 100 characters."),
		)

		mockMvc.perform(
			post("/financial-schedules")
				.contentType(MediaType.APPLICATION_JSON)
				.content(
					"""
					{
					  "type": "LOAN",
					  "title": " $title ",
					  "amount": 475000,
					  "direction": "EXPENSE",
					  "recurrence": { "type": "ONCE", "scheduledDate": "2026-01-01" }
					}
					""".trimIndent(),
				),
		)
			.andExpect(status().isBadRequest)
			.andExpect(jsonPath("$.data.code").value("VALIDATION_ERROR"))
	}

	@Test
	fun `PATCH에서 누락된 필드는 Absent로 전달한다`() {
		given(financialScheduleService.patch(anyValue(scheduleId), anyValue(PatchFinancialScheduleCommand()))).willReturn(monthlyResult(title = "변경"))

		mockMvc.perform(
			patch("/financial-schedules/{id}", scheduleId)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""{ "title": "변경" }"""),
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.data.title").value("변경"))

		val captor = ArgumentCaptor.forClass(PatchFinancialScheduleCommand::class.java)
		then(financialScheduleService).should().patch(eqValue(scheduleId), captureValue(captor, PatchFinancialScheduleCommand()))
		val command = captor.value
		check(command.title == PatchField.Present("변경"))
		check(command.type == PatchField.Absent)
		check(command.amount == PatchField.Absent)
		check(command.direction == PatchField.Absent)
		check(command.recurrence == PatchField.Absent)
	}

	@Test
	fun `PATCH의 명시적 null은 서비스 검증 오류 400으로 반환한다`() {
		given(financialScheduleService.patch(anyValue(scheduleId), anyValue(PatchFinancialScheduleCommand()))).willThrow(InvalidFinancialScheduleException("title must not be null."))

		mockMvc.perform(
			patch("/financial-schedules/{id}", scheduleId)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""{ "title": null }"""),
		)
			.andExpect(status().isBadRequest)
			.andExpect(jsonPath("$.data.code").value("VALIDATION_ERROR"))

		val captor = ArgumentCaptor.forClass(PatchFinancialScheduleCommand::class.java)
		then(financialScheduleService).should().patch(eqValue(scheduleId), captureValue(captor, PatchFinancialScheduleCommand()))
		check(captor.value.title == PatchField.Present(null))
	}

	@Test
	fun `PATCH recurrence는 명시적 종료일 null을 포함해 전체 규칙으로 전달한다`() {
		given(financialScheduleService.patch(anyValue(scheduleId), anyValue(PatchFinancialScheduleCommand()))).willReturn(monthlyResult())

		mockMvc.perform(
			patch("/financial-schedules/{id}", scheduleId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(
					"""
					{
					  "recurrence": {
					    "type": "MONTHLY",
					    "dayOfMonth": 15,
					    "startDate": "2026-02-01",
					    "endDate": null
					  }
					}
					""".trimIndent(),
				),
		)
			.andExpect(status().isOk)

		val captor = ArgumentCaptor.forClass(PatchFinancialScheduleCommand::class.java)
		then(financialScheduleService).should().patch(eqValue(scheduleId), captureValue(captor, PatchFinancialScheduleCommand()))
		val command = captor.value
		check(
			command.recurrence == PatchField.Present(
				RecurrenceCommand(
					type = "MONTHLY",
					dayOfMonth = 15,
					startDate = LocalDate.of(2026, 2, 1),
					endDate = null,
				),
			),
		)
		check(command.type == PatchField.Absent)
		check(command.title == PatchField.Absent)
		check(command.amount == PatchField.Absent)
		check(command.direction == PatchField.Absent)
	}

	@Test
	fun `빈 PATCH는 EMPTY_PATCH 400을 반환한다`() {
		given(financialScheduleService.patch(anyValue(scheduleId), anyValue(PatchFinancialScheduleCommand()))).willThrow(
			com.knowave.cashboard.common.exception.EmptyFinancialSchedulePatchException(),
		)

		mockMvc.perform(
			patch("/financial-schedules/{id}", scheduleId)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"),
		)
			.andExpect(status().isBadRequest)
			.andExpect(jsonPath("$.data.code").value("EMPTY_PATCH"))
	}

	@Test
	fun `잘못된 recurrence 날짜는 INVALID_DATE_FORMAT 400을 반환한다`() {
		mockMvc.perform(
			post("/financial-schedules")
				.contentType(MediaType.APPLICATION_JSON)
				.content(
					"""{ "type": "LOAN", "title": "일정", "amount": 1000, "direction": "EXPENSE", "recurrence": { "type": "ONCE", "scheduledDate": "invalid-date" } }""",
				),
		)
			.andExpect(status().isBadRequest)
			.andExpect(jsonPath("$.data.code").value("INVALID_DATE_FORMAT"))
	}

	@Test
	fun `문법적으로 손상된 JSON은 MALFORMED_REQUEST 400을 반환한다`() {
		mockMvc.perform(
			post("/financial-schedules")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{"),
		)
			.andExpect(status().isBadRequest)
			.andExpect(jsonPath("$.data.code").value("MALFORMED_REQUEST"))
	}

	@Test
	fun `목록과 단건 조회는 성공 응답을 반환한다`() {
		given(financialScheduleService.getAll()).willReturn(listOf(monthlyResult()))
		given(financialScheduleService.get(scheduleId)).willReturn(monthlyResult())

		mockMvc.perform(get("/financial-schedules"))
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.data[0].id").value(scheduleId.toString()))
		mockMvc.perform(get("/financial-schedules/{id}", scheduleId))
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.data.id").value(scheduleId.toString()))
	}

	@Test
	fun `삭제는 성공 true를 반환한다`() {
		mockMvc.perform(delete("/financial-schedules/{id}", scheduleId))
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data").value(true))
	}

	@Test
	fun `서비스의 NotFoundException은 404로 반환한다`() {
		given(financialScheduleService.get(scheduleId)).willThrow(NotFoundException("FinancialSchedule", scheduleId))

		mockMvc.perform(get("/financial-schedules/{id}", scheduleId))
			.andExpect(status().isNotFound)
			.andExpect(jsonPath("$.data.code").value("NOT_FOUND"))
	}

	private fun monthlyResult(title: String = "신용대출 상환") = FinancialScheduleResult(
		id = scheduleId,
		type = "LOAN",
		title = title,
		amount = 475_000,
		direction = "EXPENSE",
		recurrence = RecurrenceResult(
			type = "MONTHLY",
			dayOfMonth = 25,
			startDate = LocalDate.of(2026, 1, 1),
		),
		createdAt = LocalDateTime.of(2026, 1, 1, 10, 0),
		updatedAt = LocalDateTime.of(2026, 1, 1, 10, 0),
	)

	private fun createCommandFallback() = com.knowave.cashboard.domains.financialschedule.service.dto.CreateFinancialScheduleCommand(
		type = "LOAN",
		title = "대체값",
		amount = 1,
		direction = "EXPENSE",
		recurrence = com.knowave.cashboard.domains.financialschedule.service.dto.RecurrenceCommand(
			type = "ONCE",
			scheduledDate = LocalDate.of(2026, 1, 1),
		),
	)

	private fun <T> anyValue(fallback: T): T = ArgumentMatchers.any<T>() ?: fallback

	private fun <T> eqValue(value: T): T = ArgumentMatchers.eq(value) ?: value

	private fun <T> captureValue(captor: ArgumentCaptor<T>, fallback: T): T = captor.capture() ?: fallback
}
