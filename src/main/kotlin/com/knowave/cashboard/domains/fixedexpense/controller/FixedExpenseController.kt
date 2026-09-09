package com.knowave.cashboard.domains.fixedexpense.controller

import com.knowave.cashboard.common.response.ApiResponse
import com.knowave.cashboard.common.response.success
import com.knowave.cashboard.common.security.AuthenticatedUser
import com.knowave.cashboard.domains.fixedexpense.controller.dto.FixedExpenseRequest
import com.knowave.cashboard.domains.fixedexpense.controller.dto.FixedExpenseResponse
import com.knowave.cashboard.domains.fixedexpense.controller.dto.toResponse
import com.knowave.cashboard.domains.fixedexpense.service.FixedExpenseService
import jakarta.validation.Valid
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/fixed-expenses")
class FixedExpenseController(
	private val fixedExpenseService: FixedExpenseService,
) {
	@GetMapping
	fun getAll(@AuthenticationPrincipal user: AuthenticatedUser): ApiResponse<List<FixedExpenseResponse>> =
		success(fixedExpenseService.getAll(user.userId).map { it.toResponse() })

	@GetMapping("/{id}")
	fun get(@AuthenticationPrincipal user: AuthenticatedUser, @PathVariable id: UUID): ApiResponse<FixedExpenseResponse> =
		success(fixedExpenseService.get(user.userId, id).toResponse())

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	fun create(@AuthenticationPrincipal user: AuthenticatedUser, @Valid @RequestBody request: FixedExpenseRequest): ApiResponse<FixedExpenseResponse> =
		success(fixedExpenseService.create(user.userId, request.toCreateCommand()).toResponse())

	@PutMapping("/{id}")
	fun update(
		@AuthenticationPrincipal user: AuthenticatedUser,
		@PathVariable id: UUID,
		@Valid @RequestBody request: FixedExpenseRequest,
	): ApiResponse<FixedExpenseResponse> =
		success(fixedExpenseService.update(user.userId, id, request.toUpdateCommand()).toResponse())

	@DeleteMapping("/{id}")
	fun delete(@AuthenticationPrincipal user: AuthenticatedUser, @PathVariable id: UUID): ApiResponse<Boolean> {
		fixedExpenseService.delete(user.userId, id)
		return success(true)
	}

}
