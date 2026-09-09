package com.knowave.cashboard.domains.budget.service

import com.knowave.cashboard.domains.budget.service.dto.BudgetExpenseResult
import com.knowave.cashboard.domains.budget.service.dto.CreateBudgetExpenseCommand
import com.knowave.cashboard.domains.budget.service.dto.CreateMonthlyBudgetCommand
import com.knowave.cashboard.domains.budget.service.dto.MonthlyBudgetResult
import com.knowave.cashboard.domains.budget.service.dto.UpdateMonthlyBudgetCommand
import com.knowave.cashboard.domains.budget.service.dto.UpdateUsedAmountCommand
import java.util.UUID

interface BudgetStrategyService {
	fun create(userId: UUID, command: CreateMonthlyBudgetCommand): MonthlyBudgetResult
	fun getByTargetMonth(userId: UUID, targetMonth: String): MonthlyBudgetResult
	fun update(userId: UUID, id: UUID, command: UpdateMonthlyBudgetCommand): MonthlyBudgetResult
	fun updateUsedAmount(userId: UUID, id: UUID, command: UpdateUsedAmountCommand): MonthlyBudgetResult
	fun addExpense(userId: UUID, id: UUID, command: CreateBudgetExpenseCommand): MonthlyBudgetResult
	fun getExpenses(userId: UUID, id: UUID): List<BudgetExpenseResult>
	fun deleteExpense(userId: UUID, monthlyBudgetId: UUID, expenseId: UUID): Boolean
}
