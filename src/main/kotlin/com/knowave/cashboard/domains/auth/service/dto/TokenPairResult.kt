package com.knowave.cashboard.domains.auth.service.dto

data class TokenPairResult(
	val accessToken: String,
	val refreshToken: String,
)
