package com.knowave.cashboard.domains.budget.controller

import com.knowave.cashboard.domains.budget.controller.dto.BudgetExpenseRequest
import com.knowave.cashboard.domains.budget.controller.dto.BudgetExpenseResponse
import com.knowave.cashboard.domains.budget.controller.dto.MonthlyBudgetRequest
import com.knowave.cashboard.domains.budget.controller.dto.MonthlyBudgetResponse
import com.knowave.cashboard.domains.budget.controller.dto.UpdateMonthlyBudgetRequest
import com.knowave.cashboard.domains.budget.controller.dto.UpdateUsedAmountRequest
import com.knowave.cashboard.domains.budget.controller.dto.toResponse
import com.knowave.cashboard.domains.budget.service.BudgetStrategyService
import com.knowave.cashboard.common.response.ApiResponse
import com.knowave.cashboard.common.response.success
import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Validated
@RestController
@RequestMapping("/monthly-budgets")
class MonthlyBudgetController(
	private val budgetStrategyService: BudgetStrategyService,
) {
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	fun create(@Valid @RequestBody request: MonthlyBudgetRequest): ApiResponse<MonthlyBudgetResponse> =
		success(budgetStrategyService.create(request.toCreateCommand()).toResponse())

	@GetMapping("/{targetMonth}")
	fun getByTargetMonth(
		@PathVariable
		@Pattern(regexp = "\\d{4}-\\d{2}", message = "targetMonth must be yyyy-MM.")
		targetMonth: String,
	): ApiResponse<MonthlyBudgetResponse> =
		success(budgetStrategyService.getByTargetMonth(targetMonth).toResponse())

	@PatchMapping("/{id}")
	fun update(
		@PathVariable id: UUID,
		@Valid @RequestBody request: UpdateMonthlyBudgetRequest,
	): ApiResponse<MonthlyBudgetResponse> =
		success(budgetStrategyService.update(id, request.toCommand()).toResponse())

	@PatchMapping("/{id}/used-amount")
	fun updateUsedAmount(
		@PathVariable id: UUID,
		@Valid @RequestBody request: UpdateUsedAmountRequest,
	): ApiResponse<MonthlyBudgetResponse> =
		success(budgetStrategyService.updateUsedAmount(id, request.toCommand()).toResponse())

	@PostMapping("/{id}/expenses")
	@ResponseStatus(HttpStatus.CREATED)
	fun addExpense(
		@PathVariable id: UUID,
		@Valid @RequestBody request: BudgetExpenseRequest,
	): ApiResponse<MonthlyBudgetResponse> =
		success(budgetStrategyService.addExpense(id, request.toCommand()).toResponse())

	@GetMapping("/{id}/expenses")
	fun getExpenses(@PathVariable id: UUID): ApiResponse<List<BudgetExpenseResponse>> =
		success(budgetStrategyService.getExpenses(id).map { it.toResponse() })

	@DeleteMapping("/{monthlyBudgetId}/expenses/{expenseId}")
	fun deleteExpense(
		@PathVariable monthlyBudgetId: UUID,
		@PathVariable expenseId: UUID,
	): ApiResponse<Boolean> =
		success(budgetStrategyService.deleteExpense(monthlyBudgetId, expenseId))
}
