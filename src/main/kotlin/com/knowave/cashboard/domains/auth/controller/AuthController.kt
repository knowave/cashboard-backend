package com.knowave.cashboard.domains.auth.controller

import com.knowave.cashboard.common.response.ApiResponse
import com.knowave.cashboard.common.response.success
import com.knowave.cashboard.domains.auth.controller.dto.LogoutRequest
import com.knowave.cashboard.domains.auth.controller.dto.RefreshRequest
import com.knowave.cashboard.domains.auth.controller.dto.SocialLoginRequest
import com.knowave.cashboard.domains.auth.controller.dto.TokenPairResponse
import com.knowave.cashboard.domains.auth.controller.dto.toResponse
import com.knowave.cashboard.domains.auth.service.AuthService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
	private val authService: AuthService,
) {
	@PostMapping("/social/login")
	fun socialLogin(@Valid @RequestBody request: SocialLoginRequest): ApiResponse<TokenPairResponse> =
		success(authService.socialLogin(request.toCommand()).toResponse())

	@PostMapping("/refresh")
	fun refresh(@Valid @RequestBody request: RefreshRequest): ApiResponse<TokenPairResponse> =
		success(authService.refresh(request.refreshToken).toResponse())

	@PostMapping("/logout")
	fun logout(@Valid @RequestBody request: LogoutRequest): ApiResponse<Boolean> {
		authService.logout(request.refreshToken, request.allDevices)
		return success(true)
	}
}
