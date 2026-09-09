package com.knowave.cashboard.domains.auth.service

import com.knowave.cashboard.domains.auth.service.dto.SocialLoginCommand
import com.knowave.cashboard.domains.auth.service.dto.TokenPairResult

interface AuthService {
	fun socialLogin(command: SocialLoginCommand): TokenPairResult

	fun refresh(refreshToken: String): TokenPairResult

	fun logout(refreshToken: String, logoutAllDevices: Boolean)
}
