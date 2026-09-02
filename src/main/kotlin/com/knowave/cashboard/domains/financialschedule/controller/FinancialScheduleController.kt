package com.knowave.cashboard.domains.financialschedule.controller

import com.knowave.cashboard.common.response.ApiResponse
import com.knowave.cashboard.common.response.success
import com.knowave.cashboard.domains.financialschedule.controller.dto.FinancialScheduleCreateRequest
import com.knowave.cashboard.domains.financialschedule.controller.dto.FinancialSchedulePatchRequest
import com.knowave.cashboard.domains.financialschedule.controller.dto.FinancialScheduleResponse
import com.knowave.cashboard.domains.financialschedule.controller.dto.toResponse
import com.knowave.cashboard.domains.financialschedule.service.FinancialScheduleService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
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

@RestController
@RequestMapping("/financial-schedules")
class FinancialScheduleController(
	private val financialScheduleService: FinancialScheduleService,
) {
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	fun create(@Valid @RequestBody request: FinancialScheduleCreateRequest) =
		success(financialScheduleService.create(request.toCommand()).toResponse())

	@GetMapping
	fun getAll() = success(financialScheduleService.getAll().map { it.toResponse() })

	@GetMapping("/{id}")
	fun get(@PathVariable id: UUID) = success(financialScheduleService.get(id).toResponse())

	@PatchMapping("/{id}")
	fun patch(@PathVariable id: UUID, @RequestBody request: FinancialSchedulePatchRequest) =
		success(financialScheduleService.patch(id, request.toCommand()).toResponse())

	@DeleteMapping("/{id}")
	fun delete(@PathVariable id: UUID): ApiResponse<Boolean> {
		financialScheduleService.delete(id)
		return success(true)
	}
}
