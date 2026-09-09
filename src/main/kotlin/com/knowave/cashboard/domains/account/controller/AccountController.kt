package com.knowave.cashboard.domains.account.controller

import com.knowave.cashboard.common.response.ApiResponse
import com.knowave.cashboard.common.response.success
import com.knowave.cashboard.common.security.AuthenticatedUser
import com.knowave.cashboard.domains.account.controller.dto.AccountRequest
import com.knowave.cashboard.domains.account.controller.dto.AccountResponse
import com.knowave.cashboard.domains.account.controller.dto.toResponse
import com.knowave.cashboard.domains.account.service.AccountService
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
@RequestMapping("/accounts")
class AccountController(
	private val accountService: AccountService,
) {
	@GetMapping
	fun getAll(@AuthenticationPrincipal user: AuthenticatedUser): ApiResponse<List<AccountResponse>> =
		success(accountService.getAllAccount(user.userId).map { it.toResponse() })

	@GetMapping("/{id}")
	fun get(@AuthenticationPrincipal user: AuthenticatedUser, @PathVariable id: UUID): ApiResponse<AccountResponse> =
		success(accountService.getAccount(user.userId, id).toResponse())

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	fun create(@AuthenticationPrincipal user: AuthenticatedUser, @Valid @RequestBody request: AccountRequest): ApiResponse<AccountResponse> =
		success(accountService.create(user.userId, request.toCreateCommand()).toResponse())

	@PutMapping("/{id}")
	fun update(
		@AuthenticationPrincipal user: AuthenticatedUser,
		@PathVariable id: UUID,
		@Valid @RequestBody request: AccountRequest,
	): ApiResponse<AccountResponse> =
		success(accountService.update(user.userId, id, request.toUpdateCommand()).toResponse())

	@DeleteMapping("/{id}")
	fun delete(@AuthenticationPrincipal user: AuthenticatedUser, @PathVariable id: UUID): ApiResponse<Boolean> {
		accountService.delete(user.userId, id)
		return success(true)
	}

}
