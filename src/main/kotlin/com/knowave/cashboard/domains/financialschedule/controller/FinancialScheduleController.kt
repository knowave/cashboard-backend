package com.knowave.cashboard.domains.financialschedule.controller

import com.knowave.cashboard.common.response.ApiResponse
import com.knowave.cashboard.common.response.success
import com.knowave.cashboard.common.security.AuthenticatedUser
import com.knowave.cashboard.domains.financialschedule.controller.dto.FinancialScheduleCreateRequest
import com.knowave.cashboard.domains.financialschedule.controller.dto.FinancialSchedulePatchRequest
import com.knowave.cashboard.domains.financialschedule.controller.dto.FinancialScheduleResponse
import com.knowave.cashboard.domains.financialschedule.controller.dto.toResponse
import com.knowave.cashboard.domains.financialschedule.service.FinancialScheduleService
import jakarta.validation.Valid
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/financial-schedules")
class FinancialScheduleController(
	private val financialScheduleService: FinancialScheduleService,
) {
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	fun create(@AuthenticationPrincipal user: AuthenticatedUser, @Valid @RequestBody request: FinancialScheduleCreateRequest) =
		success(financialScheduleService.create(user.userId, request.toCommand()).toResponse())

	@GetMapping
	fun getAll(@AuthenticationPrincipal user: AuthenticatedUser) = success(financialScheduleService.getAll(user.userId).map { it.toResponse() })

	@GetMapping("/{id}")
	fun get(@AuthenticationPrincipal user: AuthenticatedUser, @PathVariable id: UUID) = success(financialScheduleService.get(user.userId, id).toResponse())

	@PatchMapping("/{id}")
	fun patch(@AuthenticationPrincipal user: AuthenticatedUser, @PathVariable id: UUID, @RequestBody request: FinancialSchedulePatchRequest) =
		success(financialScheduleService.patch(user.userId, id, request.toCommand()).toResponse())

	@DeleteMapping("/{id}")
	fun delete(@AuthenticationPrincipal user: AuthenticatedUser, @PathVariable id: UUID): ApiResponse<Boolean> {
		financialScheduleService.delete(user.userId, id)
		return success(true)
	}

}
