package com.knowave.cashboard.domains.budget.entity

import com.knowave.cashboard.common.entity.BaseEntity
import com.knowave.cashboard.domains.budget.service.dto.UpdateMonthlyBudgetCommand
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "monthly_budgets")
class MonthlyBudget(
	@Column(name = "target_month", nullable = false, unique = true, length = 7)
	var targetMonth: String,

	@Column(name = "monthly_budget", nullable = false)
	var monthlyBudget: Long,

	@Column(name = "used_amount", nullable = false)
	var usedAmount: Long = 0,
) : BaseEntity() {
	fun updateUsedAmount(usedAmount: Long) {
		this.usedAmount = usedAmount
	}

	fun addUsedAmount(amount: Long) {
		this.usedAmount += amount
	}

	fun subtractUsedAmount(amount: Long) {
		this.usedAmount = (usedAmount - amount).coerceAtLeast(0)
	}

	companion object {
		fun applyUpdate(monthlyBudget: MonthlyBudget, command: UpdateMonthlyBudgetCommand): MonthlyBudget {
			if (monthlyBudget.targetMonth != command.targetMonth) {
				monthlyBudget.targetMonth = command.targetMonth
			}
			if (monthlyBudget.monthlyBudget != command.monthlyBudget) {
				monthlyBudget.monthlyBudget = command.monthlyBudget
			}
			if (monthlyBudget.usedAmount != command.usedAmount) {
				monthlyBudget.usedAmount = command.usedAmount
			}
			return monthlyBudget
		}
	}
}
