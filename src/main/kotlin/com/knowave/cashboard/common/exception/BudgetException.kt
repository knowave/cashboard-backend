package com.knowave.cashboard.common.exception

import org.springframework.http.HttpStatus
import java.util.UUID

class MonthlyBudgetNotFoundException(value: Any) : CashboardException(
	errorCode = "MONTHLY_BUDGET_NOT_FOUND",
	message = "MonthlyBudget not found. value=$value",
	status = HttpStatus.NOT_FOUND,
)

class DuplicateMonthlyBudgetException(targetMonth: String) : CashboardException(
	errorCode = "DUPLICATE_MONTHLY_BUDGET",
	message = "MonthlyBudget already exists. targetMonth=$targetMonth",
	status = HttpStatus.CONFLICT,
)

class BudgetExpenseNotFoundException(id: UUID) : CashboardException(
	errorCode = "BUDGET_EXPENSE_NOT_FOUND",
	message = "BudgetExpense not found. id=$id",
	status = HttpStatus.NOT_FOUND,
)

class InvalidBudgetExpenseException(expenseId: UUID, monthlyBudgetId: UUID) : CashboardException(
	errorCode = "INVALID_BUDGET_EXPENSE",
	message = "BudgetExpense does not belong to MonthlyBudget. expenseId=$expenseId, monthlyBudgetId=$monthlyBudgetId",
	status = HttpStatus.BAD_REQUEST,
)

class InvalidTargetMonthException(targetMonth: String) : CashboardException(
	errorCode = "INVALID_TARGET_MONTH",
	message = "targetMonth must be valid yyyy-MM. targetMonth=$targetMonth",
	status = HttpStatus.BAD_REQUEST,
)
