package com.knowave.cashboard.domains.expenseanalysis.repository.dto

interface MonthlyExpenseProjection {
    val yearMonth: String
    val amount: Long
}
