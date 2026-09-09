package com.knowave.cashboard.domains.auth.social

import com.knowave.cashboard.domains.auth.exception.ProviderNotConfiguredException
import com.knowave.cashboard.domains.auth.exception.SocialAuthenticationFailedException
import com.knowave.cashboard.domains.auth.exception.SocialAuthProviderUnavailableException
import com.knowave.cashboard.domains.auth.exception.UnsupportedSocialLoginException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.oauth2.jwt.JwtException
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

/**
 * Apple만 userinfo 엔드포인트가 없다. 교환 응답의 id_token 클레임에서 sub/email을 읽는다.
 * nickname·profile_image를 주지 않으므로 둘 다 null로 반환한다 — nickname은
 * UserService.getOrCreate가 채운다.
 */
@Component
class AppleSocialAuthProvider(
	private val restClient: RestClient,
	private val clientSecretGenerator: AppleClientSecretGenerator,
	private val identityTokenVerifier: AppleIdentityTokenVerifier,
	@Value("\${auth.apple.client-id}") private val clientId: String,
	@Value("\${auth.apple.team-id}") private val teamId: String,
	@Value("\${auth.apple.key-id}") private val keyId: String,
	@Value("\${auth.apple.private-key-b64}") private val privateKeyPemBase64: String,
	@Value("\${auth.apple.redirect-uri.ios}") private val redirectUriIos: String,
	@Value("\${auth.apple.token-uri}") private val tokenUri: String,
) : SocialAuthProvider {
	private val logger = LoggerFactory.getLogger(javaClass)

	init {
		if (!isConfigured()) {
			logger.warn("Social login provider is not configured. provider={}", SocialProvider.APPLE)
		}
	}

	override fun supports(provider: SocialProvider): Boolean = provider == SocialProvider.APPLE

	override fun authenticate(authorizationCode: String, pkceCodeVerifier: String, platform: ClientPlatform): SocialUserInfo {
		if (!isConfigured()) throw ProviderNotConfiguredException(SocialProvider.APPLE)

		val tokenResponse = ProviderHttpSupport.exchangeToken(
			restClient = restClient,
			logger = logger,
			provider = SocialProvider.APPLE,
			tokenUri = tokenUri,
			clientId = clientId,
			clientSecret = clientSecretGenerator.generate(),
			authorizationCode = authorizationCode,
			pkceCodeVerifier = pkceCodeVerifier,
			redirectUri = redirectUriFor(platform),
		)
		val idToken = tokenResponse.idToken ?: run {
			logger.warn("Social login provider returned an unexpected response. provider={}", SocialProvider.APPLE)
			throw SocialAuthProviderUnavailableException(SocialProvider.APPLE)
		}
		val identityTokenClaims = try {
			identityTokenVerifier.verify(idToken)
		} catch (exception: JwtException) {
			logger.warn("Apple id_token verification failed. provider={}", SocialProvider.APPLE)
			throw SocialAuthenticationFailedException(SocialProvider.APPLE)
		}

		return SocialUserInfo(
			provider = SocialProvider.APPLE,
			providerId = identityTokenClaims.subject,
			email = identityTokenClaims.getClaimAsString("email"),
			nickname = null,
			profileImageUrl = null,
		)
	}

	private fun isConfigured(): Boolean = teamId.isNotBlank() && keyId.isNotBlank() && privateKeyPemBase64.isNotBlank()

	private fun redirectUriFor(platform: ClientPlatform): String = when (platform) {
		ClientPlatform.IOS -> redirectUriIos
		// ponytail: ANDROID+APPLE은 SocialLoginPolicy가 먼저 거부한다. 여기 도달하면 정책이
		// 어긋난 것이므로 방어적으로만 막는다.
		ClientPlatform.ANDROID -> throw UnsupportedSocialLoginException(platform, SocialProvider.APPLE)
	}
}
