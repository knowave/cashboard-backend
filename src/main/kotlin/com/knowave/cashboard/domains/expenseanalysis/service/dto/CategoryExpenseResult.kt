package com.knowave.cashboard.domains.expenseanalysis.service.dto

data class CategoryExpenseResult(
    val category: String,
    val amount: Long,
    val ratio: Double,
)
