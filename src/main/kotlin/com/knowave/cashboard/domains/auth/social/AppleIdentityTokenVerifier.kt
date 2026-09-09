package com.knowave.cashboard.domains.auth.social

import org.springframework.beans.factory.annotation.Value
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimNames
import org.springframework.security.oauth2.jwt.JwtClaimValidator
import org.springframework.security.oauth2.jwt.JwtIssuerValidator
import org.springframework.security.oauth2.jwt.JwtTimestampValidator
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.stereotype.Component

/**
 * Apple id_token 서명 검증 전용. code 교환이 백채널이어도 검증을 생략하지 않는다 (OIDC Core 3.1.3.7).
 */
@Component
class AppleIdentityTokenVerifier(
	@Value("\${auth.apple.jwks-uri}") jwksUri: String,
	@Value("\${auth.apple.client-id}") clientId: String,
) {
	// ponytail: 빈이 아니라 private 필드. 컨텍스트에 JwtDecoder 빈이 2개면(자체 발급 HS256용과
	// 이것) 주입이 모호해져 부팅이 죽는다.
	private val decoder = NimbusJwtDecoder.withJwkSetUri(jwksUri).build().apply {
		setJwtValidator(
			DelegatingOAuth2TokenValidator(
				JwtTimestampValidator(),
				JwtIssuerValidator("https://appleid.apple.com"),
				JwtClaimValidator<List<String>>(JwtClaimNames.AUD) { clientId in it },
			),
		)
	}

	fun verify(identityToken: String): Jwt = decoder.decode(identityToken)
}
