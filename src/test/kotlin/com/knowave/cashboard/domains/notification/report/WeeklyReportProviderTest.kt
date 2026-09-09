package com.knowave.cashboard.domains.notification.report

import com.knowave.cashboard.domains.budget.entity.MonthlyBudget
import com.knowave.cashboard.domains.budget.repository.MonthlyBudgetRepository
import com.knowave.cashboard.domains.expenseanalysis.repository.ExpenseAnalysisRepository
import com.knowave.cashboard.domains.expenseanalysis.repository.dto.CategoryExpenseProjection
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

class WeeklyReportProviderTest {
	private val userId = UUID.randomUUID()
	private val expenseRepository = WeeklyExpenseRepository()
	private val monthlyBudgetRepository = WeeklyMonthlyBudgetRepository()
	private val provider = WeeklyReportProvider(expenseRepository, monthlyBudgetRepository)

	@Test
	fun `월요일에는 직전 월요일부터 일요일까지 집계하고 전주와 비교한다`() {
		expenseRepository.expensesByStart[LocalDate.of(2026, 8, 31)] = listOf(category("식비", 80_000), category("교통", 20_000))
		expenseRepository.expensesByStart[LocalDate.of(2026, 8, 24)] = listOf(category("식비", 50_000))
		monthlyBudgetRepository.budget = MonthlyBudget(userId, "2026-09", 500_000, 120_000)

		val summary = provider.generate(userId, LocalDate.of(2026, 9, 7))

		assertThat(summary.weekStart).isEqualTo(LocalDate.of(2026, 8, 31))
		assertThat(expenseRepository.requestedRanges).containsExactly(
			LocalDate.of(2026, 8, 31) to LocalDate.of(2026, 9, 7),
			LocalDate.of(2026, 8, 24) to LocalDate.of(2026, 8, 31),
		)
		assertThat(summary.totalExpense).isEqualTo(100_000)
		assertThat(summary.previousWeekDifferenceRate).isEqualTo(100.0)
		assertThat(summary.topCategory).isEqualTo("식비")
		assertThat(summary.remainingBudget).isEqualTo(380_000)
	}

	@Test
	fun `동률 카테고리는 이름 오름차순으로 선택하고 비교나 예산이 없으면 생략한다`() {
		expenseRepository.expensesByStart[LocalDate.of(2026, 8, 31)] = listOf(category("교통", 20_000), category("식비", 20_000))

		val summary = provider.generate(userId, LocalDate.of(2026, 9, 7))

		assertThat(summary.topCategory).isEqualTo("교통")
		assertThat(summary.previousWeekDifferenceRate).isNull()
		assertThat(summary.remainingBudget).isNull()
	}

	@Test
	fun `지출이 없는 주는 지출 없음 메시지를 만든다`() {
		val summary = provider.generate(userId, LocalDate.of(2026, 9, 7))

		assertThat(summary.totalExpense).isZero()
		assertThat(summary.toMessage()).isEqualTo("지난주에는 지출이 없었어요.")
	}

	private fun category(category: String, amount: Long) = object : CategoryExpenseProjection {
		override val category = category
		override val amount = amount
	}
}

private class WeeklyExpenseRepository : ExpenseAnalysisRepository {
	val expensesByStart = mutableMapOf<LocalDate, List<CategoryExpenseProjection>>()
	val requestedRanges = mutableListOf<Pair<LocalDate, LocalDate>>()

	override fun findCategoryExpenses(userId: UUID, start: LocalDate, end: LocalDate): List<CategoryExpenseProjection> {
		requestedRanges += start to end
		return expensesByStart[start].orEmpty()
	}

	override fun findMonthlyExpenses(userId: UUID, start: LocalDate, end: LocalDate) = emptyList<com.knowave.cashboard.domains.expenseanalysis.repository.dto.MonthlyExpenseProjection>()
}

private class WeeklyMonthlyBudgetRepository : MonthlyBudgetRepository {
	var budget: MonthlyBudget? = null
	override fun save(monthlyBudget: MonthlyBudget) = monthlyBudget
	override fun findByIdAndUserId(id: UUID, userId: UUID) = null
	override fun findByIdForUpdate(id: UUID, userId: UUID) = null
	override fun findByTargetMonthAndUserId(targetMonth: String, userId: UUID) = budget?.takeIf { it.targetMonth == targetMonth }
	override fun existsByIdAndUserId(id: UUID, userId: UUID) = false
	override fun existsByTargetMonthAndUserId(targetMonth: String, userId: UUID) = false
}
