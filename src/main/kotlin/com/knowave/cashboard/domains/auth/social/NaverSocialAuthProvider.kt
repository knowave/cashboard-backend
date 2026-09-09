package com.knowave.cashboard.domains.auth.social

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.knowave.cashboard.domains.auth.exception.ProviderNotConfiguredException
import com.knowave.cashboard.domains.auth.exception.SocialAuthProviderUnavailableException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class NaverSocialAuthProvider(
	private val restClient: RestClient,
	@Value("\${auth.naver.client-id}") private val clientId: String,
	@Value("\${auth.naver.client-secret}") private val clientSecret: String,
	@Value("\${auth.naver.redirect-uri.ios}") private val redirectUriIos: String,
	@Value("\${auth.naver.redirect-uri.android}") private val redirectUriAndroid: String,
	@Value("\${auth.naver.token-uri}") private val tokenUri: String,
	@Value("\${auth.naver.userinfo-uri}") private val userinfoUri: String,
) : SocialAuthProvider {
	private val logger = LoggerFactory.getLogger(javaClass)

	init {
		if (clientSecret.isBlank()) {
			logger.warn("Social login provider is not configured. provider={}", SocialProvider.NAVER)
		}
	}

	override fun supports(provider: SocialProvider): Boolean = provider == SocialProvider.NAVER

	override fun authenticate(authorizationCode: String, pkceCodeVerifier: String, platform: ClientPlatform): SocialUserInfo {
		if (clientSecret.isBlank()) throw ProviderNotConfiguredException(SocialProvider.NAVER)

		val tokenResponse = ProviderHttpSupport.exchangeToken(
			restClient = restClient,
			logger = logger,
			provider = SocialProvider.NAVER,
			tokenUri = tokenUri,
			clientId = clientId,
			clientSecret = clientSecret,
			authorizationCode = authorizationCode,
			pkceCodeVerifier = pkceCodeVerifier,
			redirectUri = redirectUriFor(platform),
		)
		val userInfoResponse = ProviderHttpSupport.callProvider(logger, SocialProvider.NAVER) {
			restClient.get()
				.uri(userinfoUri)
				.header("Authorization", "Bearer ${tokenResponse.accessToken}")
				.retrieve()
				.body(NaverUserInfoResponse::class.java)
		}
		val userInfoBody = userInfoResponse.response ?: run {
			logger.warn("Social login provider returned an unexpected response. provider={}", SocialProvider.NAVER)
			throw SocialAuthProviderUnavailableException(SocialProvider.NAVER)
		}

		return SocialUserInfo(
			provider = SocialProvider.NAVER,
			providerId = userInfoBody.id,
			email = userInfoBody.email,
			nickname = userInfoBody.nickname,
			profileImageUrl = userInfoBody.profileImage,
		)
	}

	private fun redirectUriFor(platform: ClientPlatform): String = when (platform) {
		ClientPlatform.IOS -> redirectUriIos
		ClientPlatform.ANDROID -> redirectUriAndroid
	}
}

@JsonIgnoreProperties(ignoreUnknown = true)
private data class NaverUserInfoResponse(@JsonProperty("response") val response: NaverUserInfoBody?)

@JsonIgnoreProperties(ignoreUnknown = true)
private data class NaverUserInfoBody(
	@JsonProperty("id") val id: String,
	@JsonProperty("email") val email: String?,
	@JsonProperty("nickname") val nickname: String?,
	@JsonProperty("profile_image") val profileImage: String?,
)
