package com.knowave.cashboard.domains.notification.report

import com.knowave.cashboard.domains.account.entity.Account
import com.knowave.cashboard.domains.account.repository.AccountRepository
import com.knowave.cashboard.domains.assetgoal.calculator.AssetGoalCalculator
import com.knowave.cashboard.domains.assetgoal.entity.AssetGoal
import com.knowave.cashboard.domains.assetgoal.repository.AssetGoalRepository
import com.knowave.cashboard.domains.expenseanalysis.service.ExpenseAnalysisService
import com.knowave.cashboard.domains.expenseanalysis.service.dto.CategoryExpenseResult
import com.knowave.cashboard.domains.expenseanalysis.service.dto.ExpenseAnalysisResult
import com.knowave.cashboard.domains.expenseanalysis.service.dto.ExpenseComparisonResult
import com.knowave.cashboard.domains.expenseanalysis.service.dto.PeriodResult
import com.knowave.cashboard.domains.expenseanalysis.service.dto.RecentAverageResult
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.util.UUID

class MonthlyReportProviderTest {
	private val expenseAnalysisService = FixedExpenseAnalysisService()
	private val assetGoalRepository = FixedAssetGoalRepository()
	private val accountRepository = FixedAccountRepository()
	private val provider = MonthlyReportProvider(expenseAnalysisService, assetGoalRepository, accountRepository, AssetGoalCalculator())

	@Test
	fun `직전 달의 상위 세 카테고리와 일평균 및 가장 가까운 목표를 요약한다`() {
		expenseAnalysisService.result = analysis(
			total = 310_000,
			differenceRate = 10.0,
			categories = listOf(
				CategoryExpenseResult("교통", 100_000, 0.0),
				CategoryExpenseResult("식비", 100_000, 0.0),
				CategoryExpenseResult("여가", 60_000, 0.0),
				CategoryExpenseResult("쇼핑", 50_000, 0.0),
			),
		)
		assetGoalRepository.goals = listOf(
			AssetGoal("먼 목표", 1_000_000, LocalDate.of(2027, 1, 1)),
			AssetGoal("가까운 목표", 1_000_000, LocalDate.of(2026, 12, 1)),
		)
		accountRepository.accounts = listOf(Account("입출금", "BANK", 400_000))

		val summary = provider.generate(LocalDate.of(2026, 9, 1))

		assertThat(expenseAnalysisService.requested).isEqualTo(2026 to 8)
		assertThat(summary.yearMonth).isEqualTo(YearMonth.of(2026, 8))
		assertThat(summary.totalExpense).isEqualTo(310_000)
		assertThat(summary.previousMonthDifferenceRate).isEqualTo(10.0)
		assertThat(summary.topCategories).containsExactly("교통", "식비", "여가")
		assertThat(summary.averageDailyExpense).isEqualTo(10_000)
		assertThat(summary.assetGoalName).isEqualTo("가까운 목표")
		assertThat(summary.assetGoalAchievementRate).isEqualTo(40.0)
	}

	@Test
	fun `분석 결과가 정렬되지 않아도 금액 내림차순과 이름 오름차순으로 상위 카테고리를 정한다`() {
		expenseAnalysisService.result = analysis(
			total = 100,
			differenceRate = null,
			categories = listOf(
				CategoryExpenseResult("식비", 30, 0.0),
				CategoryExpenseResult("교통", 30, 0.0),
				CategoryExpenseResult("여가", 40, 0.0),
			),
		)

		val summary = provider.generate(LocalDate.of(2026, 9, 1))

		assertThat(summary.topCategories).containsExactly("여가", "교통", "식비")
		assertThat(summary.previousMonthDifferenceRate).isNull()
		assertThat(summary.assetGoalName).isNull()
	}

	@Test
	fun `지출이 없는 달은 지출 없음 메시지를 만든다`() {
		expenseAnalysisService.result = analysis(0, null, emptyList())

		val summary = provider.generate(LocalDate.of(2026, 9, 1))

		assertThat(summary.toMessage()).isEqualTo("지난달에는 지출이 없었어요.")
	}

	@Test
	fun `현재 자산 합계가 Long 범위를 넘으면 계산을 중단한다`() {
		accountRepository.accounts = listOf(
			Account("첫 계좌", "BANK", Long.MAX_VALUE),
			Account("두 번째 계좌", "BANK", 1),
		)

		assertThatThrownBy { provider.generate(LocalDate.of(2026, 9, 1)) }
			.isInstanceOf(ArithmeticException::class.java)
	}

	@Test
	fun `같은 목표일에서는 생성 시각과 ID 오름차순으로 목표를 선택하고 null은 마지막에 둔다`() {
		val sameDate = LocalDate.of(2026, 12, 1)
		val nullMetadata = goal("null 메타데이터", sameDate, null, null)
		val laterCreated = goal("늦게 생성", sameDate, LocalDateTime.of(2026, 1, 2, 0, 0), UUID.fromString("00000000-0000-0000-0000-000000000001"))
		val laterId = goal("큰 ID", sameDate, LocalDateTime.of(2026, 1, 1, 0, 0), UUID.fromString("00000000-0000-0000-0000-000000000002"))
		val earlierId = goal("작은 ID", sameDate, LocalDateTime.of(2026, 1, 1, 0, 0), UUID.fromString("00000000-0000-0000-0000-000000000001"))
		assetGoalRepository.goals = listOf(nullMetadata, laterCreated, laterId, earlierId)

		val summary = provider.generate(LocalDate.of(2026, 9, 1))

		assertThat(summary.assetGoalName).isEqualTo("작은 ID")
	}

	@Test
	fun `같은 목표일에서 createdAt이 null인 목표는 생성 시각이 있는 목표보다 뒤에 둔다`() {
		val targetDate = LocalDate.of(2026, 12, 1)
		assetGoalRepository.goals = listOf(
			goal("생성 시각 없음", targetDate, null, UUID.fromString("00000000-0000-0000-0000-000000000001")),
			goal("생성 시각 있음", targetDate, LocalDateTime.of(2026, 1, 1, 0, 0), null),
		)

		val summary = provider.generate(LocalDate.of(2026, 9, 1))

		assertThat(summary.assetGoalName).isEqualTo("생성 시각 있음")
	}

	private fun analysis(total: Long, differenceRate: Double?, categories: List<CategoryExpenseResult>) = ExpenseAnalysisResult(
		period = PeriodResult(2026, 8),
		totalExpense = total,
		previousMonthComparison = ExpenseComparisonResult(0, 0, differenceRate),
		recentAverage = RecentAverageResult(0, 0),
		categories = categories,
		trend = emptyList(),
	)

	private fun goal(name: String, targetDate: LocalDate, createdAt: LocalDateTime?, id: UUID?): AssetGoal =
		AssetGoal(name, 1_000_000, targetDate).also {
			it.createdAt = createdAt
			val idField = it.javaClass.superclass.getDeclaredField("id")
			idField.isAccessible = true
			idField.set(it, id)
		}
}

private class FixedExpenseAnalysisService : ExpenseAnalysisService {
	var requested: Pair<Int, Int>? = null
	var result = ExpenseAnalysisResult(PeriodResult(2026, 8), 0, ExpenseComparisonResult(0, 0, null), RecentAverageResult(0, 0), emptyList(), emptyList())
	override fun getAnalysis(year: Int, month: Int): ExpenseAnalysisResult {
		requested = year to month
		return result
	}
}

private class FixedAssetGoalRepository : AssetGoalRepository {
	var goals: List<AssetGoal> = emptyList()
	override fun save(assetGoal: AssetGoal) = assetGoal
	override fun findById(id: java.util.UUID) = null
	override fun findAll() = goals
	override fun delete(assetGoal: AssetGoal) = Unit
}

private class FixedAccountRepository : AccountRepository {
	var accounts: List<Account> = emptyList()
	override fun save(account: Account) = account
	override fun findById(id: java.util.UUID) = null
	override fun findAll() = accounts
	override fun delete(account: Account) = Unit
}
