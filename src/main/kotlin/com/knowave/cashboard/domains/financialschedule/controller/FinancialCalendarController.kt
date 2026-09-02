package com.knowave.cashboard.domains.financialschedule.controller

import com.knowave.cashboard.common.response.ApiResponse
import com.knowave.cashboard.common.response.success
import com.knowave.cashboard.domains.financialschedule.controller.dto.FinancialCalendarResponse
import com.knowave.cashboard.domains.financialschedule.controller.dto.toResponse
import com.knowave.cashboard.domains.financialschedule.service.FinancialCalendarService
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/financial-calendar")
class FinancialCalendarController(
	private val financialCalendarService: FinancialCalendarService,
) {
	@GetMapping
	fun getCalendar(
		@RequestParam @Min(1900) @Max(9999) year: Int,
		@RequestParam @Min(1) @Max(12) month: Int,
	): ApiResponse<FinancialCalendarResponse> =
		success(financialCalendarService.getCalendar(year, month).toResponse())
}
