package com.knowave.cashboard.domains.auth.service.dto

import com.knowave.cashboard.domains.auth.social.ClientPlatform
import com.knowave.cashboard.domains.auth.social.SocialProvider

data class SocialLoginCommand(
	val provider: SocialProvider,
	val platform: ClientPlatform,
	val authorizationCode: String,
	val pkceCodeVerifier: String,
	val timezone: String,
)
