package com.knowave.cashboard.domains.expenseanalysis.service.dto

data class ExpenseComparisonResult(
    val previousAmount: Long,
    val differenceAmount: Long,
    val differenceRate: Double?,
)
