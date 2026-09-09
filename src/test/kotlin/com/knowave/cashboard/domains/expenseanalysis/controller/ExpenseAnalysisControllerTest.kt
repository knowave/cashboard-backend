package com.knowave.cashboard.domains.expenseanalysis.controller

import com.knowave.cashboard.domains.expenseanalysis.service.ExpenseAnalysisService
import com.knowave.cashboard.domains.expenseanalysis.service.dto.CategoryExpenseResult
import com.knowave.cashboard.domains.expenseanalysis.service.dto.ExpenseAnalysisResult
import com.knowave.cashboard.domains.expenseanalysis.service.dto.ExpenseComparisonResult
import com.knowave.cashboard.domains.expenseanalysis.service.dto.MonthlyExpenseResult
import com.knowave.cashboard.domains.expenseanalysis.service.dto.PeriodResult
import com.knowave.cashboard.domains.expenseanalysis.service.dto.RecentAverageResult
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import com.knowave.cashboard.support.WithAuthenticatedUser
import com.knowave.cashboard.common.config.ClockConfig
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

// ponytail: 슬라이스 테스트는 매핑·검증을 검증한다. 보안 체인은 전용 테스트의 몫이므로
// 필터를 끈다 — @WebMvcTest는 우리 SecurityConfig를 로드하지 않고 Boot 기본 설정을
// 쓰기 때문에 CSRF가 켜져 비-GET이 403을 받는다. @AuthenticationPrincipal은
// SecurityContextHolder에서 해석되므로 필터를 꺼도 @WithAuthenticatedUser가 동작한다.
@AutoConfigureMockMvc(addFilters = false)
@WithAuthenticatedUser
@Import(ClockConfig::class)
@WebMvcTest(ExpenseAnalysisController::class)
class ExpenseAnalysisControllerTest {

	@Autowired
	private lateinit var mockMvc: MockMvc

	@MockitoBean
	private lateinit var expenseAnalysisService: ExpenseAnalysisService

	// Controller의 TEMP_USER_ID(TODO Stage 2)와 동일한 placeholder.
	private val tempUserId = UUID.fromString("00000000-0000-0000-0000-000000000000")

	private fun fixtureResult(): ExpenseAnalysisResult = ExpenseAnalysisResult(
		period = PeriodResult(year = 2026, month = 8),
		totalExpense = 700000L,
		previousMonthComparison = ExpenseComparisonResult(
			previousAmount = 500000L,
			differenceAmount = 200000L,
			differenceRate = 40.0,
		),
		recentAverage = RecentAverageResult(amount = 600000L, months = 3),
		categories = listOf(
			CategoryExpenseResult(category = "food", amount = 700000L, ratio = 100.0),
		),
		trend = listOf(
			MonthlyExpenseResult(yearMonth = "2026-08", amount = 700000L),
		),
	)

	@Test
	fun `유효한 year와 month면 200과 분석 결과를 반환한다`() {
		given(expenseAnalysisService.getAnalysis(tempUserId, 2026, 8)).willReturn(fixtureResult())

		mockMvc.perform(get("/expense-analysis").param("year", "2026").param("month", "8"))
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.totalExpense").value(700000))
	}

	@Test
	fun `year가 1900 미만이면 400을 반환한다`() {
		mockMvc.perform(get("/expense-analysis").param("year", "1899").param("month", "8"))
			.andExpect(status().isBadRequest)
	}

	@Test
	fun `month가 12 초과이면 400을 반환한다`() {
		mockMvc.perform(get("/expense-analysis").param("year", "2026").param("month", "13"))
			.andExpect(status().isBadRequest)
	}

	@Test
	fun `year 파라미터가 없으면 400을 반환한다`() {
		mockMvc.perform(get("/expense-analysis").param("month", "8"))
			.andExpect(status().isBadRequest)
	}

	@Test
	fun `year 파라미터 타입이 올바르지 않으면 400을 반환한다`() {
		mockMvc.perform(get("/expense-analysis").param("year", "abc").param("month", "8"))
			.andExpect(status().isBadRequest)
	}
}
