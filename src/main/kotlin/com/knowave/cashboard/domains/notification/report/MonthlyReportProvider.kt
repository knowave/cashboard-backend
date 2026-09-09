package com.knowave.cashboard.domains.notification.report

import com.knowave.cashboard.domains.account.repository.AccountRepository
import com.knowave.cashboard.domains.assetgoal.calculator.AssetGoalCalculator
import com.knowave.cashboard.domains.assetgoal.entity.AssetGoal
import com.knowave.cashboard.domains.assetgoal.repository.AssetGoalRepository
import com.knowave.cashboard.domains.expenseanalysis.service.ExpenseAnalysisService
import com.knowave.cashboard.domains.expenseanalysis.service.dto.CategoryExpenseResult
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

@Component
class MonthlyReportProvider(
	private val expenseAnalysisService: ExpenseAnalysisService,
	private val assetGoalRepository: AssetGoalRepository,
	private val accountRepository: AccountRepository,
	private val assetGoalCalculator: AssetGoalCalculator,
) {
	fun generate(userId: UUID, runDate: LocalDate): MonthlyReportSummary {
		val yearMonth = YearMonth.from(runDate).minusMonths(1)
		val analysis = expenseAnalysisService.getAnalysis(userId, yearMonth.year, yearMonth.monthValue)
		val currentAssets = accountRepository.findAllByUserId(userId).fold(0L) { total, account ->
			Math.addExact(total, account.balance)
		}
		val goal = assetGoalRepository.findAllByUserId(userId).sortedWith(
			compareBy<AssetGoal> { it.targetDate }
				.thenBy(nullsLast()) { it.createdAt }
				.thenBy(nullsLast()) { it.id },
		).firstOrNull()
		val achievementRate = goal?.let {
			assetGoalCalculator.calculate(it.targetAmount, currentAssets, it.targetDate, emptyList(), runDate).achievementRate
		}

		return MonthlyReportSummary(
			yearMonth = yearMonth,
			totalExpense = analysis.totalExpense,
			previousMonthDifferenceRate = analysis.previousMonthComparison.differenceRate,
			topCategories = analysis.categories.sortedWith(
				compareByDescending<CategoryExpenseResult> { it.amount }.thenBy { it.category },
			).take(3).map { it.category },
			averageDailyExpense = analysis.totalExpense / yearMonth.lengthOfMonth(),
			assetGoalName = goal?.name,
			assetGoalAchievementRate = achievementRate,
		)
	}
}

data class MonthlyReportSummary(
	val yearMonth: YearMonth,
	val totalExpense: Long,
	val previousMonthDifferenceRate: Double?,
	val topCategories: List<String>,
	val averageDailyExpense: Long,
	val assetGoalName: String?,
	val assetGoalAchievementRate: Double?,
) {
	fun toMessage(): String {
		if (totalExpense == 0L) return "지난달에는 지출이 없었어요."

		return buildList {
			add("지난달 총 지출은 ${totalExpense}원이에요")
			add("일평균 지출은 ${averageDailyExpense}원이에요")
			previousMonthDifferenceRate?.let { add("전월 대비 ${it}% 변화했어요") }
			if (topCategories.isNotEmpty()) add("상위 카테고리는 ${topCategories.joinToString(", ")}예요")
			assetGoalAchievementRate?.let { add("${assetGoalName} 목표 달성률은 ${it}%예요") }
		}.joinToString(", ") + "."
	}
}
