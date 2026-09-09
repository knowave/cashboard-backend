package com.knowave.cashboard.domains.auth.controller.dto

import com.knowave.cashboard.common.exception.InvalidEnumValueException
import com.knowave.cashboard.domains.auth.service.dto.SocialLoginCommand
import com.knowave.cashboard.domains.auth.social.ClientPlatform
import com.knowave.cashboard.domains.auth.social.SocialProvider
import jakarta.validation.constraints.NotBlank

data class SocialLoginRequest(
	@field:NotBlank(message = "provider is required.")
	val provider: String,

	@field:NotBlank(message = "platform is required.")
	val platform: String,

	@field:NotBlank(message = "code is required.")
	val code: String,

	@field:NotBlank(message = "codeVerifier is required.")
	val codeVerifier: String,

	@field:NotBlank(message = "timezone is required.")
	val timezone: String,
) {
	fun toCommand(): SocialLoginCommand = SocialLoginCommand(
		provider = toProvider(),
		platform = toPlatform(),
		authorizationCode = code,
		pkceCodeVerifier = codeVerifier,
		timezone = timezone,
	)

	private fun toProvider(): SocialProvider = runCatching { SocialProvider.valueOf(provider.trim().uppercase()) }
		.getOrElse { throw InvalidEnumValueException("SocialProvider", provider) }

	private fun toPlatform(): ClientPlatform = runCatching { ClientPlatform.valueOf(platform.trim().uppercase()) }
		.getOrElse { throw InvalidEnumValueException("ClientPlatform", platform) }
}
