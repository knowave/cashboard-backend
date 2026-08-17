package com.knowave.cashboard.domains.expenseanalysis.controller

import com.knowave.cashboard.domains.expenseanalysis.controller.dto.ExpenseAnalysisResponse
import com.knowave.cashboard.domains.expenseanalysis.controller.dto.toResponse
import com.knowave.cashboard.domains.expenseanalysis.service.ExpenseAnalysisService
import com.knowave.cashboard.common.response.ApiResponse
import com.knowave.cashboard.common.response.success
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
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
		@RequestParam @Min(1900) @Max(9999) year: Int,
		@RequestParam @Min(1) @Max(12) month: Int,
	): ApiResponse<ExpenseAnalysisResponse> =
		success(expenseAnalysisService.getAnalysis(year, month).toResponse())
}
