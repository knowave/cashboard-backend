package com.knowave.cashboard.domains.expenseanalysis.controller

import com.knowave.cashboard.common.response.ApiResponse
import com.knowave.cashboard.common.response.success
import com.knowave.cashboard.common.security.AuthenticatedUser
import com.knowave.cashboard.domains.expenseanalysis.controller.dto.ExpenseAnalysisResponse
import com.knowave.cashboard.domains.expenseanalysis.controller.dto.toResponse
import com.knowave.cashboard.domains.expenseanalysis.service.ExpenseAnalysisService
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import java.util.UUID
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/expense-analysis")
class ExpenseAnalysisController(
	private val expenseAnalysisService: ExpenseAnalysisService,
) {
	@GetMapping
	fun getAnalysis(
		@AuthenticationPrincipal user: AuthenticatedUser,
		@RequestParam @Min(1900) @Max(9999) year: Int,
		@RequestParam @Min(1) @Max(12) month: Int,
	): ApiResponse<ExpenseAnalysisResponse> =
		success(expenseAnalysisService.getAnalysis(user.userId, year, month).toResponse())

}
