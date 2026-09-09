package com.knowave.cashboard.domains.budget.controller

import com.knowave.cashboard.common.response.ApiResponse
import com.knowave.cashboard.common.response.success
import com.knowave.cashboard.common.security.AuthenticatedUser
import com.knowave.cashboard.domains.budget.controller.dto.BudgetExpenseRequest
import com.knowave.cashboard.domains.budget.controller.dto.BudgetExpenseResponse
import com.knowave.cashboard.domains.budget.controller.dto.MonthlyBudgetRequest
import com.knowave.cashboard.domains.budget.controller.dto.MonthlyBudgetResponse
import com.knowave.cashboard.domains.budget.controller.dto.UpdateMonthlyBudgetRequest
import com.knowave.cashboard.domains.budget.controller.dto.UpdateUsedAmountRequest
import com.knowave.cashboard.domains.budget.controller.dto.toResponse
import com.knowave.cashboard.domains.budget.service.BudgetStrategyService
import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
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

@Validated
@RestController
@RequestMapping("/monthly-budgets")
class MonthlyBudgetController(
	private val budgetStrategyService: BudgetStrategyService,
) {
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	fun create(@AuthenticationPrincipal user: AuthenticatedUser, @Valid @RequestBody request: MonthlyBudgetRequest): ApiResponse<MonthlyBudgetResponse> =
		success(budgetStrategyService.create(user.userId, request.toCreateCommand()).toResponse())

	@GetMapping("/{targetMonth}")
	fun getByTargetMonth(
		@AuthenticationPrincipal user: AuthenticatedUser,
		@PathVariable
		@Pattern(regexp = "\\d{4}-\\d{2}", message = "targetMonth must be yyyy-MM.")
		targetMonth: String,
	): ApiResponse<MonthlyBudgetResponse> =
		success(budgetStrategyService.getByTargetMonth(user.userId, targetMonth).toResponse())

	@PatchMapping("/{id}")
	fun update(
		@AuthenticationPrincipal user: AuthenticatedUser,
		@PathVariable id: UUID,
		@Valid @RequestBody request: UpdateMonthlyBudgetRequest,
	): ApiResponse<MonthlyBudgetResponse> =
		success(budgetStrategyService.update(user.userId, id, request.toCommand()).toResponse())

	@PatchMapping("/{id}/used-amount")
	fun updateUsedAmount(
		@AuthenticationPrincipal user: AuthenticatedUser,
		@PathVariable id: UUID,
		@Valid @RequestBody request: UpdateUsedAmountRequest,
	): ApiResponse<MonthlyBudgetResponse> =
		success(budgetStrategyService.updateUsedAmount(user.userId, id, request.toCommand()).toResponse())

	@PostMapping("/{id}/expenses")
	@ResponseStatus(HttpStatus.CREATED)
	fun addExpense(
		@AuthenticationPrincipal user: AuthenticatedUser,
		@PathVariable id: UUID,
		@Valid @RequestBody request: BudgetExpenseRequest,
	): ApiResponse<MonthlyBudgetResponse> =
		success(budgetStrategyService.addExpense(user.userId, id, request.toCommand()).toResponse())

	@GetMapping("/{id}/expenses")
	fun getExpenses(@AuthenticationPrincipal user: AuthenticatedUser, @PathVariable id: UUID): ApiResponse<List<BudgetExpenseResponse>> =
		success(budgetStrategyService.getExpenses(user.userId, id).map { it.toResponse() })

	@DeleteMapping("/{monthlyBudgetId}/expenses/{expenseId}")
	fun deleteExpense(
		@AuthenticationPrincipal user: AuthenticatedUser,
		@PathVariable monthlyBudgetId: UUID,
		@PathVariable expenseId: UUID,
	): ApiResponse<Boolean> =
		success(budgetStrategyService.deleteExpense(user.userId, monthlyBudgetId, expenseId))

}
