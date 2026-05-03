package com.knowave.cashboard.domains.fixedexpense.entity

import com.knowave.cashboard.common.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.YearMonth

@Entity
@Table(name = "fixed_expenses")
class FixedExpense(
	@Column(name = "name", nullable = false)
	var name: String,

	@Column(name = "amount", nullable = false)
	var amount: Long,

	@Column(name = "category", nullable = false, length = 100)
	var category: String,

	@Column(name = "start_month", nullable = false, length = 7)
	var startMonth: String,

	@Column(name = "end_month", length = 7)
	var endMonth: String? = null,
) : BaseEntity() {
	fun update(name: String, amount: Long, category: String, startMonth: YearMonth, endMonth: YearMonth?) {
		this.name = name
		this.amount = amount
		this.category = category
		this.startMonth = startMonth.toString()
		this.endMonth = endMonth?.toString()
	}
}
