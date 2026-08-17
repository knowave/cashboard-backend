package com.knowave.cashboard.domains.expenseanalysis.service

import com.knowave.cashboard.domains.expenseanalysis.repository.ExpenseAnalysisRepository
import com.knowave.cashboard.domains.expenseanalysis.repository.dto.CategoryExpenseProjection
import com.knowave.cashboard.domains.expenseanalysis.repository.dto.MonthlyExpenseProjection
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDate

// CategoryExpenseProjection / MonthlyExpenseProjection are val-only Kotlin interfaces
// (JPA projection interfaces with no default implementation), so plain local data
// classes stand in for them as test fixtures.
private data class FakeCategoryExpenseProjection(
	override val category: String,
	override val amount: Long,
) : CategoryExpenseProjection

private data class FakeMonthlyExpenseProjection(
	override val yearMonth: String,
	override val amount: Long,
) : MonthlyExpenseProjection

@ExtendWith(MockitoExtension::class)
class ExpenseAnalysisServiceImplTest {

	@Mock
	private lateinit var expenseAnalysisRepository: ExpenseAnalysisRepository

	@InjectMocks
	private lateinit var expenseAnalysisServiceImpl: ExpenseAnalysisServiceImpl

	// NOTE: category-null-to-"UNCATEGORIZED" bucketing and categories/trend sort order
	// are intentionally NOT covered here. Those are SQL-level concerns (query/mapping
	// behavior), verified separately via a manual smoke script, out of scope for this
	// mock-based Service unit test.

	@Test
	fun `지출이 없으면 총액은 0이고 카테고리 목록은 비어있다`() {
		given(expenseAnalysisRepository.findCategoryExpenses(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 9, 1)))
			.willReturn(emptyList())
		given(expenseAnalysisRepository.findMonthlyExpenses(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 9, 1)))
			.willReturn(emptyList())

		val result = expenseAnalysisServiceImpl.getAnalysis(2026, 8)

		assertThat(result.totalExpense).isEqualTo(0L)
		assertThat(result.categories).isEmpty()
	}

	@Test
	fun `이전 달 데이터가 없으면 previousAmount는 0이고 differenceRate는 null이다`() {
		given(expenseAnalysisRepository.findCategoryExpenses(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 9, 1)))
			.willReturn(listOf(FakeCategoryExpenseProjection("food", 700000)))
		// monthly window has data, but none for the previous month (2026-07)
		given(expenseAnalysisRepository.findMonthlyExpenses(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 9, 1)))
			.willReturn(listOf(FakeMonthlyExpenseProjection("2026-06", 100000)))

		val result = expenseAnalysisServiceImpl.getAnalysis(2026, 8)

		assertThat(result.totalExpense).isEqualTo(700000L)
		assertThat(result.previousMonthComparison.previousAmount).isEqualTo(0L)
		assertThat(result.previousMonthComparison.differenceAmount).isEqualTo(result.totalExpense)
		assertThat(result.previousMonthComparison.differenceRate).isNull()
	}

	@Test
	fun `최근 3개월 중 데이터가 하나도 없으면 recentAverage는 0개월 평균 0이다`() {
		given(expenseAnalysisRepository.findCategoryExpenses(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 9, 1)))
			.willReturn(emptyList())
		given(expenseAnalysisRepository.findMonthlyExpenses(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 9, 1)))
			.willReturn(emptyList())

		val result = expenseAnalysisServiceImpl.getAnalysis(2026, 8)

		assertThat(result.recentAverage.months).isEqualTo(0)
		assertThat(result.recentAverage.amount).isEqualTo(0L)
	}

	@Test
	fun `최근 3개월 중 1개월만 데이터가 있으면 recentAverage는 해당 1개월 값 그대로다`() {
		given(expenseAnalysisRepository.findCategoryExpenses(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 9, 1)))
			.willReturn(emptyList())
		given(expenseAnalysisRepository.findMonthlyExpenses(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 9, 1)))
			.willReturn(listOf(FakeMonthlyExpenseProjection("2026-07", 300000)))

		val result = expenseAnalysisServiceImpl.getAnalysis(2026, 8)

		assertThat(result.recentAverage.months).isEqualTo(1)
		assertThat(result.recentAverage.amount).isEqualTo(300000L)
	}

	@Test
	fun `최근 3개월 중 2개월만 데이터가 있으면 recentAverage는 2개월 평균을 정수로 내림한 값이다`() {
		given(expenseAnalysisRepository.findCategoryExpenses(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 9, 1)))
			.willReturn(emptyList())
		given(expenseAnalysisRepository.findMonthlyExpenses(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 9, 1)))
			.willReturn(
				listOf(
					FakeMonthlyExpenseProjection("2026-07", 100001),
					FakeMonthlyExpenseProjection("2026-06", 100002),
				),
			)

		val result = expenseAnalysisServiceImpl.getAnalysis(2026, 8)

		// (100001 + 100002) / 2 = 100001 (integer division truncates the remainder)
		assertThat(result.recentAverage.months).isEqualTo(2)
		assertThat(result.recentAverage.amount).isEqualTo(100001L)
	}

	@Test
	fun `연도가 바뀌는 1월 조회 시 monthly 조회 구간이 전년도까지 걸쳐있고 전월 비교도 올바르다`() {
		given(expenseAnalysisRepository.findCategoryExpenses(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 1)))
			.willReturn(listOf(FakeCategoryExpenseProjection("food", 500000)))
		// windowStart = 2026-01 minus max(TREND_MONTHS-1, RECENT_AVERAGE_MONTHS)=5 months = 2025-08
		given(expenseAnalysisRepository.findMonthlyExpenses(LocalDate.of(2025, 8, 1), LocalDate.of(2026, 2, 1)))
			.willReturn(listOf(FakeMonthlyExpenseProjection("2025-12", 400000)))

		val result = expenseAnalysisServiceImpl.getAnalysis(2026, 1)

		verify(expenseAnalysisRepository).findMonthlyExpenses(LocalDate.of(2025, 8, 1), LocalDate.of(2026, 2, 1))
		assertThat(result.totalExpense).isEqualTo(500000L)
		assertThat(result.previousMonthComparison.previousAmount).isEqualTo(400000L)
		assertThat(result.previousMonthComparison.differenceAmount).isEqualTo(100000L)
		assertThat(result.previousMonthComparison.differenceRate).isEqualTo(25.0)
	}

	@Test
	fun `카테고리 ratio는 HALF_UP 정책으로 소수 둘째자리까지 반올림된다`() {
		given(expenseAnalysisRepository.findCategoryExpenses(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 4, 1)))
			.willReturn(
				listOf(
					FakeCategoryExpenseProjection("food", 320000),
					FakeCategoryExpenseProjection("transport", 540000),
				),
			)
		given(expenseAnalysisRepository.findMonthlyExpenses(LocalDate.of(2025, 10, 1), LocalDate.of(2026, 4, 1)))
			.willReturn(emptyList())

		val result = expenseAnalysisServiceImpl.getAnalysis(2026, 3)

		// 320000 * 100.0 / 860000 = 37.2093... -> HALF_UP -> 37.21
		val foodRatio = result.categories.first { it.category == "food" }.ratio
		assertThat(foodRatio).isEqualTo(37.21)
	}

	@Test
	fun `differenceRate가 음수여도 HALF_UP 정책으로 소수 둘째자리까지 반올림된다`() {
		given(expenseAnalysisRepository.findCategoryExpenses(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 4, 1)))
			.willReturn(listOf(FakeCategoryExpenseProjection("food", 860000)))
		given(expenseAnalysisRepository.findMonthlyExpenses(LocalDate.of(2025, 10, 1), LocalDate.of(2026, 4, 1)))
			.willReturn(listOf(FakeMonthlyExpenseProjection("2026-02", 920000)))

		val result = expenseAnalysisServiceImpl.getAnalysis(2026, 3)

		// (860000 - 920000) * 100.0 / 920000 = -6.52173... -> HALF_UP -> -6.52
		assertThat(result.previousMonthComparison.differenceRate).isEqualTo(-6.52)
	}
}
