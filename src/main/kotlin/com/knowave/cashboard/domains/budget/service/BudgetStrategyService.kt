package com.knowave.cashboard.domains.budget.service

import com.knowave.cashboard.domains.budget.service.dto.BudgetExpenseResult
import com.knowave.cashboard.domains.budget.service.dto.CreateBudgetExpenseCommand
import com.knowave.cashboard.domains.budget.service.dto.CreateMonthlyBudgetCommand
import com.knowave.cashboard.domains.budget.service.dto.MonthlyBudgetResult
import com.knowave.cashboard.domains.budget.service.dto.UpdateMonthlyBudgetCommand
import com.knowave.cashboard.domains.budget.service.dto.UpdateUsedAmountCommand
import java.util.UUID

interface BudgetStrategyService {
	fun create(command: CreateMonthlyBudgetCommand): MonthlyBudgetResult
	fun getByTargetMonth(targetMonth: String): MonthlyBudgetResult
	fun update(id: UUID, command: UpdateMonthlyBudgetCommand): MonthlyBudgetResult
	fun updateUsedAmount(id: UUID, command: UpdateUsedAmountCommand): MonthlyBudgetResult
	fun addExpense(id: UUID, command: CreateBudgetExpenseCommand): MonthlyBudgetResult
	fun getExpenses(id: UUID): List<BudgetExpenseResult>
	fun deleteExpense(monthlyBudgetId: UUID, expenseId: UUID): Boolean
}
