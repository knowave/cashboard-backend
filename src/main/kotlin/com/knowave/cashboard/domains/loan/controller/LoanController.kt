package com.knowave.cashboard.domains.loan.controller

import com.knowave.cashboard.common.response.ApiResponse
import com.knowave.cashboard.common.response.success
import com.knowave.cashboard.domains.loan.controller.dto.LoanRequest
import com.knowave.cashboard.domains.loan.controller.dto.LoanResponse
import com.knowave.cashboard.domains.loan.controller.dto.toResponse
import com.knowave.cashboard.domains.loan.service.LoanService
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
@RequestMapping("/loans")
class LoanController(
	private val loanService: LoanService,
) {
	@GetMapping
	fun getAll(): ApiResponse<List<LoanResponse>> =
		success(loanService.getAll().map { it.toResponse() })

	@GetMapping("/{id}")
	fun get(@PathVariable id: UUID): ApiResponse<LoanResponse> =
		success(loanService.get(id).toResponse())

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	fun create(@Valid @RequestBody request: LoanRequest): ApiResponse<LoanResponse> =
		success(loanService.create(request.toCreateCommand()).toResponse())

	@PutMapping("/{id}")
	fun update(
		@PathVariable id: UUID,
		@Valid @RequestBody request: LoanRequest,
	): ApiResponse<LoanResponse> =
		success(loanService.update(id, request.toUpdateCommand()).toResponse())

	@DeleteMapping("/{id}")
	fun delete(@PathVariable id: UUID): ApiResponse<Boolean> {
		loanService.delete(id)
		return success(true)
	}
}
