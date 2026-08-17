package com.knowave.cashboard.domains.expenseanalysis.service.dto

data class PeriodResult(val year: Int, val month: Int)
data class RecentAverageResult(val amount: Long, val months: Int)

data class ExpenseAnalysisResult(
    val period: PeriodResult,
    val totalExpense: Long,
    val previousMonthComparison: ExpenseComparisonResult,
    val recentAverage: RecentAverageResult,
    val categories: List<CategoryExpenseResult>,
    val trend: List<MonthlyExpenseResult>,
)
