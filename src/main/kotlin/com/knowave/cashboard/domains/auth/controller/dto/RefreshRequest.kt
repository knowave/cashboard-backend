package com.knowave.cashboard.domains.auth.controller.dto

import jakarta.validation.constraints.NotBlank

data class RefreshRequest(
	@field:NotBlank(message = "refreshToken is required.")
	val refreshToken: String,
)
