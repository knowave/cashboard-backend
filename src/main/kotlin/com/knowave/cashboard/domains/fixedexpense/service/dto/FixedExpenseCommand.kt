package com.knowave.cashboard.domains.fixedexpense.service.dto

import com.knowave.cashboard.domains.fixedexpense.entity.FixedExpense
import java.time.YearMonth

data class CreateFixedExpenseCommand(
	val name: String,
	val amount: Long,
	val category: String,
	val startMonth: YearMonth,
	val endMonth: YearMonth?,
) {
	fun toEntity(): FixedExpense = FixedExpense(
		name = name,
		amount = amount,
		category = category,
		startMonth = startMonth.toString(),
		endMonth = endMonth?.toString(),
	)
}

data class UpdateFixedExpenseCommand(
	val name: String,
	val amount: Long,
	val category: String,
	val startMonth: YearMonth,
	val endMonth: YearMonth?,
)
