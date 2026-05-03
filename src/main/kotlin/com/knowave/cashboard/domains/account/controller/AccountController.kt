package com.knowave.cashboard.domains.account.controller

import com.knowave.cashboard.common.response.ApiResponse
import com.knowave.cashboard.common.response.success
import com.knowave.cashboard.domains.account.controller.dto.AccountRequest
import com.knowave.cashboard.domains.account.controller.dto.AccountResponse
import com.knowave.cashboard.domains.account.controller.dto.toResponse
import com.knowave.cashboard.domains.account.service.AccountService
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
@RequestMapping("/accounts")
class AccountController(
	private val accountService: AccountService,
) {
	@GetMapping
	fun getAll(): ApiResponse<List<AccountResponse>> =
		success(accountService.getAllAccount().map { it.toResponse() })

	@GetMapping("/{id}")
	fun get(@PathVariable id: UUID): ApiResponse<AccountResponse> =
		success(accountService.getAccount(id).toResponse())

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	fun create(@Valid @RequestBody request: AccountRequest): ApiResponse<AccountResponse> =
		success(accountService.create(request.toCreateCommand()).toResponse())

	@PutMapping("/{id}")
	fun update(
		@PathVariable id: UUID,
		@Valid @RequestBody request: AccountRequest,
	): ApiResponse<AccountResponse> =
		success(accountService.update(id, request.toUpdateCommand()).toResponse())

	@DeleteMapping("/{id}")
	fun delete(@PathVariable id: UUID): ApiResponse<Boolean> {
		accountService.delete(id)
		return success(true)
	}
}
