package com.knowave.cashboard.domains.budget.entity

import com.knowave.cashboard.common.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "budget_expenses")
class BudgetExpense(
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "monthly_budget_id", nullable = false)
	var monthlyBudget: MonthlyBudget,

	@Column(name = "amount", nullable = false)
	var amount: Long,

	@Column(name = "category", length = 50)
	var category: String? = null,

	@Column(name = "memo", length = 255)
	var memo: String? = null,

	@Column(name = "spent_at", nullable = false)
	var spentAt: LocalDate,
) : BaseEntity()
