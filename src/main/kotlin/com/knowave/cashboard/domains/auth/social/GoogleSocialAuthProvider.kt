package com.knowave.cashboard.domains.auth.social

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.knowave.cashboard.domains.auth.exception.ProviderNotConfiguredException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class GoogleSocialAuthProvider(
	private val restClient: RestClient,
	@Value("\${auth.google.client-id}") private val clientId: String,
	@Value("\${auth.google.client-secret}") private val clientSecret: String,
	@Value("\${auth.google.redirect-uri.ios}") private val redirectUriIos: String,
	@Value("\${auth.google.redirect-uri.android}") private val redirectUriAndroid: String,
	@Value("\${auth.google.token-uri}") private val tokenUri: String,
	@Value("\${auth.google.userinfo-uri}") private val userinfoUri: String,
) : SocialAuthProvider {
	private val logger = LoggerFactory.getLogger(javaClass)

	init {
		if (clientSecret.isBlank()) {
			logger.warn("Social login provider is not configured. provider={}", SocialProvider.GOOGLE)
		}
	}

	override fun supports(provider: SocialProvider): Boolean = provider == SocialProvider.GOOGLE

	override fun authenticate(authorizationCode: String, pkceCodeVerifier: String, platform: ClientPlatform): SocialUserInfo {
		if (clientSecret.isBlank()) throw ProviderNotConfiguredException(SocialProvider.GOOGLE)

		val tokenResponse = ProviderHttpSupport.exchangeToken(
			restClient = restClient,
			logger = logger,
			provider = SocialProvider.GOOGLE,
			tokenUri = tokenUri,
			clientId = clientId,
			clientSecret = clientSecret,
			authorizationCode = authorizationCode,
			pkceCodeVerifier = pkceCodeVerifier,
			redirectUri = redirectUriFor(platform),
		)
		val userInfoResponse = ProviderHttpSupport.callProvider(logger, SocialProvider.GOOGLE) {
			restClient.get()
				.uri(userinfoUri)
				.header("Authorization", "Bearer ${tokenResponse.accessToken}")
				.retrieve()
				.body(GoogleUserInfoResponse::class.java)
		}

		return SocialUserInfo(
			provider = SocialProvider.GOOGLE,
			providerId = userInfoResponse.sub,
			email = userInfoResponse.email,
			nickname = userInfoResponse.name,
			profileImageUrl = userInfoResponse.picture,
		)
	}

	private fun redirectUriFor(platform: ClientPlatform): String = when (platform) {
		ClientPlatform.IOS -> redirectUriIos
		ClientPlatform.ANDROID -> redirectUriAndroid
	}
}

@JsonIgnoreProperties(ignoreUnknown = true)
private data class GoogleUserInfoResponse(
	@JsonProperty("sub") val sub: String,
	@JsonProperty("email") val email: String?,
	@JsonProperty("name") val name: String?,
	@JsonProperty("picture") val picture: String?,
)
