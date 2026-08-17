package com.knowave.cashboard.domains.expenseanalysis.controller.dto

import com.knowave.cashboard.domains.expenseanalysis.service.dto.CategoryExpenseResult
import com.knowave.cashboard.domains.expenseanalysis.service.dto.ExpenseAnalysisResult
import com.knowave.cashboard.domains.expenseanalysis.service.dto.ExpenseComparisonResult
import com.knowave.cashboard.domains.expenseanalysis.service.dto.MonthlyExpenseResult
import com.knowave.cashboard.domains.expenseanalysis.service.dto.PeriodResult
import com.knowave.cashboard.domains.expenseanalysis.service.dto.RecentAverageResult

data class PeriodResponse(
	val year: Int,
	val month: Int,
)

data class ExpenseComparisonResponse(
	val previousAmount: Long,
	val differenceAmount: Long,
	val differenceRate: Double?,
)

data class RecentAverageResponse(
	val amount: Long,
	val months: Int,
)

data class CategoryExpenseResponse(
	val category: String,
	val amount: Long,
	val ratio: Double,
)

data class MonthlyExpenseResponse(
	val yearMonth: String,
	val amount: Long,
)

data class ExpenseAnalysisResponse(
	val period: PeriodResponse,
	val totalExpense: Long,
	val previousMonthComparison: ExpenseComparisonResponse,
	val recentAverage: RecentAverageResponse,
	val categories: List<CategoryExpenseResponse>,
	val trend: List<MonthlyExpenseResponse>,
)

fun PeriodResult.toResponse(): PeriodResponse = PeriodResponse(year = year, month = month)

fun ExpenseComparisonResult.toResponse(): ExpenseComparisonResponse = ExpenseComparisonResponse(
	previousAmount = previousAmount,
	differenceAmount = differenceAmount,
	differenceRate = differenceRate,
)

fun RecentAverageResult.toResponse(): RecentAverageResponse = RecentAverageResponse(amount = amount, months = months)

fun CategoryExpenseResult.toResponse(): CategoryExpenseResponse = CategoryExpenseResponse(
	category = category,
	amount = amount,
	ratio = ratio,
)

fun MonthlyExpenseResult.toResponse(): MonthlyExpenseResponse = MonthlyExpenseResponse(yearMonth = yearMonth, amount = amount)

fun ExpenseAnalysisResult.toResponse(): ExpenseAnalysisResponse = ExpenseAnalysisResponse(
	period = period.toResponse(),
	totalExpense = totalExpense,
	previousMonthComparison = previousMonthComparison.toResponse(),
	recentAverage = recentAverage.toResponse(),
	categories = categories.map { it.toResponse() },
	trend = trend.map { it.toResponse() },
)
