package com.knowave.cashboard.domains.auth.controller.dto

import com.knowave.cashboard.domains.auth.service.dto.TokenPairResult

data class TokenPairResponse(
	val accessToken: String,
	val refreshToken: String,
)

fun TokenPairResult.toResponse(): TokenPairResponse = TokenPairResponse(
	accessToken = accessToken,
	refreshToken = refreshToken,
)
