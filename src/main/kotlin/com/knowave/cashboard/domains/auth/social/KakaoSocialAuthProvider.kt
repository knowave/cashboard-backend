package com.knowave.cashboard.domains.auth.social

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.knowave.cashboard.domains.auth.exception.ProviderNotConfiguredException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class KakaoSocialAuthProvider(
	private val restClient: RestClient,
	@Value("\${auth.kakao.client-id}") private val clientId: String,
	@Value("\${auth.kakao.client-secret}") private val clientSecret: String,
	@Value("\${auth.kakao.redirect-uri.ios}") private val redirectUriIos: String,
	@Value("\${auth.kakao.redirect-uri.android}") private val redirectUriAndroid: String,
	@Value("\${auth.kakao.token-uri}") private val tokenUri: String,
	@Value("\${auth.kakao.userinfo-uri}") private val userinfoUri: String,
) : SocialAuthProvider {
	private val logger = LoggerFactory.getLogger(javaClass)

	init {
		if (clientSecret.isBlank()) {
			logger.warn("Social login provider is not configured. provider={}", SocialProvider.KAKAO)
		}
	}

	override fun supports(provider: SocialProvider): Boolean = provider == SocialProvider.KAKAO

	override fun authenticate(authorizationCode: String, pkceCodeVerifier: String, platform: ClientPlatform): SocialUserInfo {
		if (clientSecret.isBlank()) throw ProviderNotConfiguredException(SocialProvider.KAKAO)

		val tokenResponse = ProviderHttpSupport.exchangeToken(
			restClient = restClient,
			logger = logger,
			provider = SocialProvider.KAKAO,
			tokenUri = tokenUri,
			clientId = clientId,
			clientSecret = clientSecret,
			authorizationCode = authorizationCode,
			pkceCodeVerifier = pkceCodeVerifier,
			redirectUri = redirectUriFor(platform),
		)
		val userInfoResponse = ProviderHttpSupport.callProvider(logger, SocialProvider.KAKAO) {
			restClient.get()
				.uri(userinfoUri)
				.header("Authorization", "Bearer ${tokenResponse.accessToken}")
				.retrieve()
				.body(KakaoUserInfoResponse::class.java)
		}

		return SocialUserInfo(
			provider = SocialProvider.KAKAO,
			providerId = userInfoResponse.id.toString(),
			email = userInfoResponse.kakaoAccount?.email,
			nickname = userInfoResponse.properties?.nickname,
			profileImageUrl = userInfoResponse.properties?.profileImage,
		)
	}

	private fun redirectUriFor(platform: ClientPlatform): String = when (platform) {
		ClientPlatform.IOS -> redirectUriIos
		ClientPlatform.ANDROID -> redirectUriAndroid
	}
}

@JsonIgnoreProperties(ignoreUnknown = true)
private data class KakaoUserInfoResponse(
	@JsonProperty("id") val id: Long,
	@JsonProperty("kakao_account") val kakaoAccount: KakaoAccount?,
	@JsonProperty("properties") val properties: KakaoProperties?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
private data class KakaoAccount(@JsonProperty("email") val email: String?)

@JsonIgnoreProperties(ignoreUnknown = true)
private data class KakaoProperties(
	@JsonProperty("nickname") val nickname: String?,
	@JsonProperty("profile_image") val profileImage: String?,
)
