package com.knowave.cashboard.domains.auth.social

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.KeyFactory
import java.security.interfaces.ECPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.time.Clock
import java.time.Duration
import java.util.Base64
import java.util.Date

/**
 * Apple 토큰 엔드포인트가 요구하는 client_secret(ES256 서명 JWT)을 만든다.
 * iss=Team ID, sub=Client ID, aud=https://appleid.apple.com.
 *
 * ponytail: 캐싱하지 않는다. ES256 서명은 로컬 연산이라 매 교환마다 새로 만들어도 비용이
 * 무시할 수준이고, 캐싱은 만료 임박 miss·동시 갱신 race라는 상태 관리 버그만 추가한다.
 * exp는 Apple 허용 최대(6개월)를 쓸 이유가 없어 5분으로 고정한다 — 유출 시 피해 창을 최소화한다.
 */
@Component
class AppleClientSecretGenerator(
	private val clock: Clock,
	@Value("\${auth.apple.team-id}") private val teamId: String,
	@Value("\${auth.apple.key-id}") private val keyId: String,
	@Value("\${auth.apple.client-id}") private val clientId: String,
	@Value("\${auth.apple.private-key-b64}") private val privateKeyPemBase64: String,
) {
	fun generate(): String {
		val now = clock.instant()
		val claims = JWTClaimsSet.Builder()
			.issuer(teamId)
			.subject(clientId)
			.audience("https://appleid.apple.com")
			.issueTime(Date.from(now))
			.expirationTime(Date.from(now.plus(Duration.ofMinutes(5))))
			.build()
		val header = JWSHeader.Builder(JWSAlgorithm.ES256).keyID(keyId).build()
		val jwt = SignedJWT(header, claims)
		jwt.sign(ECDSASigner(privateKey()))
		return jwt.serialize()
	}

	private fun privateKey(): ECPrivateKey {
		val derBase64 = String(Base64.getDecoder().decode(privateKeyPemBase64))
			.replace("-----BEGIN PRIVATE KEY-----", "")
			.replace("-----END PRIVATE KEY-----", "")
			.replace("\\s".toRegex(), "")
		val keySpec = PKCS8EncodedKeySpec(Base64.getDecoder().decode(derBase64))
		return KeyFactory.getInstance("EC").generatePrivate(keySpec) as ECPrivateKey
	}
}
