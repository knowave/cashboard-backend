package com.knowave.cashboard.domains.financialschedule.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.knowave.cashboard.common.exception.ProjectionRangeExceededException
import com.knowave.cashboard.domains.financialschedule.calculator.CashFlowSummary
import com.knowave.cashboard.domains.financialschedule.calculator.DailyBalance
import com.knowave.cashboard.domains.financialschedule.calculator.ScheduleOccurrence
import com.knowave.cashboard.domains.financialschedule.entity.CashFlowDirection
import com.knowave.cashboard.domains.financialschedule.entity.ScheduleType
import com.knowave.cashboard.domains.financialschedule.service.FinancialCalendarService
import com.knowave.cashboard.domains.financialschedule.service.dto.FinancialCalendarProjectionResult
import com.knowave.cashboard.domains.financialschedule.service.dto.FinancialCalendarResult
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate
import java.util.UUID

@WebMvcTest(FinancialCalendarController::class)
class FinancialCalendarControllerTest {

	@Autowired
	private lateinit var mockMvc: MockMvc

	private val objectMapper = ObjectMapper()

	@MockitoBean
	private lateinit var financialCalendarService: FinancialCalendarService

	@Test
	fun `미래 월 일정 요약 예상 잔액을 응답 계약으로 반환한다`() {
		given(financialCalendarService.getCalendar(2026, 10)).willReturn(calendarResult())

		mockMvc.perform(get("/financial-calendar").param("year", "2026").param("month", "10"))
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.year").value(2026))
			.andExpect(jsonPath("$.data.month").value(10))
			.andExpect(jsonPath("$.data.items[0].scheduleId").value("00000000-0000-0000-0000-000000000001"))
			.andExpect(jsonPath("$.data.items[0].date").value("2026-10-05"))
			.andExpect(jsonPath("$.data.items[0].type").value("SALARY"))
			.andExpect(jsonPath("$.data.items[0].title").value("급여"))
			.andExpect(jsonPath("$.data.items[0].direction").value("INCOME"))
			.andExpect(jsonPath("$.data.items[0].amount").value(3_100_000))
			.andExpect(jsonPath("$.data.items[1].scheduleId").value("00000000-0000-0000-0000-000000000002"))
			.andExpect(jsonPath("$.data.items[1].date").value("2026-10-25"))
			.andExpect(jsonPath("$.data.items[1].type").value("LOAN"))
			.andExpect(jsonPath("$.data.items[1].title").value("대출 상환"))
			.andExpect(jsonPath("$.data.items[1].direction").value("EXPENSE"))
			.andExpect(jsonPath("$.data.items[1].amount").value(250_000))
			.andExpect(jsonPath("$.data.summary.scheduledIncomeAmount").value(3_100_000))
			.andExpect(jsonPath("$.data.summary.scheduledExpenseAmount").value(250_000))
			.andExpect(jsonPath("$.data.summary.netScheduledCashFlow").value(2_850_000))
			.andExpect(jsonPath("$.data.projection.baseDate").value("2026-09-15"))
			.andExpect(jsonPath("$.data.projection.projectionStartDate").value("2026-10-01"))
			.andExpect(jsonPath("$.data.projection.projectionStartBalance").value(2_555_000))
			.andExpect(jsonPath("$.data.projection.projectedIncomeAmount").value(3_100_000))
			.andExpect(jsonPath("$.data.projection.projectedExpenseAmount").value(250_000))
			.andExpect(jsonPath("$.data.projection.projectedNetCashFlow").value(2_850_000))
			.andExpect(jsonPath("$.data.projection.expectedClosingBalance").value(5_405_000))
			.andExpect(jsonPath("$.data.projection.dailyBalances[0].date").value("2026-10-05"))
			.andExpect(jsonPath("$.data.projection.dailyBalances[0].incomeAmount").value(3_100_000))
			.andExpect(jsonPath("$.data.projection.dailyBalances[0].expenseAmount").value(0))
			.andExpect(jsonPath("$.data.projection.dailyBalances[0].expectedClosingBalance").value(5_655_000))
			.andExpect(jsonPath("$.data.projection.dailyBalances[1].date").value("2026-10-25"))
			.andExpect(jsonPath("$.data.projection.dailyBalances[1].incomeAmount").value(0))
			.andExpect(jsonPath("$.data.projection.dailyBalances[1].expenseAmount").value(250_000))
			.andExpect(jsonPath("$.data.projection.dailyBalances[1].expectedClosingBalance").value(5_405_000))
	}

	@Test
	fun `과거 월은 projection null을 그대로 반환한다`() {
		given(financialCalendarService.getCalendar(2026, 8)).willReturn(
			calendarResult().copy(year = 2026, month = 8, projection = null),
		)

		val response = mockMvc.perform(get("/financial-calendar").param("year", "2026").param("month", "8"))
			.andExpect(status().isOk)
			.andReturn()

		val data = objectMapper.readTree(response.response.contentAsString).path("data")
		check(data.has("projection"))
		check(data.path("projection").isNull)
	}

	@Test
	fun `필수 쿼리 파라미터가 누락되면 VALIDATION_ERROR 400을 반환한다`() {
		mockMvc.perform(get("/financial-calendar").param("year", "2026"))
			.andExpect(status().isBadRequest)
			.andExpect(jsonPath("$.data.code").value("VALIDATION_ERROR"))
	}

	@Test
	fun `월 파라미터가 범위를 벗어나면 VALIDATION_ERROR 400을 반환한다`() {
		mockMvc.perform(get("/financial-calendar").param("year", "2026").param("month", "13"))
			.andExpect(status().isBadRequest)
			.andExpect(jsonPath("$.data.code").value("VALIDATION_ERROR"))
	}

	@Test
	fun `연도 파라미터가 범위를 벗어나면 VALIDATION_ERROR 400을 반환한다`() {
		mockMvc.perform(get("/financial-calendar").param("year", "1899").param("month", "1"))
			.andExpect(status().isBadRequest)
			.andExpect(jsonPath("$.data.code").value("VALIDATION_ERROR"))
	}

	@Test
	fun `projection 범위 초과는 PROJECTION_RANGE_EXCEEDED 400을 반환한다`() {
		given(financialCalendarService.getCalendar(2076, 10)).willThrow(ProjectionRangeExceededException(601))

		mockMvc.perform(get("/financial-calendar").param("year", "2076").param("month", "10"))
			.andExpect(status().isBadRequest)
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.data.code").value("PROJECTION_RANGE_EXCEEDED"))
	}

	private fun calendarResult(): FinancialCalendarResult {
		val salary = ScheduleOccurrence(
			scheduleId = UUID.fromString("00000000-0000-0000-0000-000000000001"),
			date = LocalDate.of(2026, 10, 5),
			type = ScheduleType.SALARY,
			title = "급여",
			amount = 3_100_000L,
			direction = CashFlowDirection.INCOME,
		)
		val loanRepayment = ScheduleOccurrence(
			scheduleId = UUID.fromString("00000000-0000-0000-0000-000000000002"),
			date = LocalDate.of(2026, 10, 25),
			type = ScheduleType.LOAN,
			title = "대출 상환",
			amount = 250_000L,
			direction = CashFlowDirection.EXPENSE,
		)
		return FinancialCalendarResult(
			year = 2026,
			month = 10,
			items = listOf(salary, loanRepayment),
			summary = CashFlowSummary(3_100_000L, 250_000L, 2_850_000L),
			projection = FinancialCalendarProjectionResult(
				baseDate = LocalDate.of(2026, 9, 15),
				projectionStartDate = LocalDate.of(2026, 10, 1),
				projectionStartBalance = 2_555_000L,
				projectedIncomeAmount = 3_100_000L,
				projectedExpenseAmount = 250_000L,
				projectedNetCashFlow = 2_850_000L,
				expectedClosingBalance = 5_405_000L,
				dailyBalances = listOf(
					DailyBalance(LocalDate.of(2026, 10, 5), 3_100_000L, 0L, 5_655_000L),
					DailyBalance(LocalDate.of(2026, 10, 25), 0L, 250_000L, 5_405_000L),
				),
			),
		)
	}
}
