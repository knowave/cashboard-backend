package com.knowave.cashboard.domains.fixedexpense.controller

import com.knowave.cashboard.common.response.ApiResponse
import com.knowave.cashboard.common.response.success
import com.knowave.cashboard.domains.fixedexpense.controller.dto.FixedExpenseRequest
import com.knowave.cashboard.domains.fixedexpense.controller.dto.FixedExpenseResponse
import com.knowave.cashboard.domains.fixedexpense.controller.dto.toResponse
import com.knowave.cashboard.domains.fixedexpense.service.FixedExpenseService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/fixed-expenses")
class FixedExpenseController(
	private val fixedExpenseService: FixedExpenseService,
) {
	@GetMapping
	fun getAll(): ApiResponse<List<FixedExpenseResponse>> =
		success(fixedExpenseService.getAll().map { it.toResponse() })

	@GetMapping("/{id}")
	fun get(@PathVariable id: UUID): ApiResponse<FixedExpenseResponse> =
		success(fixedExpenseService.get(id).toResponse())

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	fun create(@Valid @RequestBody request: FixedExpenseRequest): ApiResponse<FixedExpenseResponse> =
		success(fixedExpenseService.create(request.toCreateCommand()).toResponse())

	@PutMapping("/{id}")
	fun update(
		@PathVariable id: UUID,
		@Valid @RequestBody request: FixedExpenseRequest,
	): ApiResponse<FixedExpenseResponse> =
		success(fixedExpenseService.update(id, request.toUpdateCommand()).toResponse())

	@DeleteMapping("/{id}")
	fun delete(@PathVariable id: UUID): ApiResponse<Boolean> {
		fixedExpenseService.delete(id)
		return success(true)
	}
}
