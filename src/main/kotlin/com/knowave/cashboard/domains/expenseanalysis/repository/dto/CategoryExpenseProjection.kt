package com.knowave.cashboard.domains.expenseanalysis.repository.dto

interface CategoryExpenseProjection {
    val category: String
    val amount: Long
}
