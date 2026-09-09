package com.knowave.cashboard.domains.auth.security

import com.knowave.cashboard.common.exception.MissingRequiredPropertyException
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.convert.DurationStyle
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Duration
import java.util.Date
import java.util.UUID

private const val MIN_SECRET_BYTES = 32

@Component
class JwtTokenProviderImpl(
	@Value("\${auth.jwt.secret}") private val jwtSecret: String,
	@Value("\${auth.jwt.issuer}") private val issuer: String,
	@Value("\${auth.jwt.access-token-ttl}") private val accessTokenTtlText: String,
	private val clock: Clock,
) : JwtTokenProvider {
	private lateinit var signer: MACSigner
	private lateinit var accessTokenTtl: Duration

	@PostConstruct
	fun init() {
		val secretBytes = jwtSecret.toByteArray()
		// .env가 비어 있으면 시크릿 없이 조용히 뜰 수 있으므로 부팅 자체를 막는다.
		if (secretBytes.size < MIN_SECRET_BYTES) {
			throw MissingRequiredPropertyException(
				"auth.jwt.secret",
				"must be set and at least $MIN_SECRET_BYTES bytes long.",
			)
		}
		signer = MACSigner(secretBytes)
		accessTokenTtl = DurationStyle.detectAndParse(accessTokenTtlText)
	}

	// 발급만 담당한다. 검증은 common/config/SecurityConfig의 NimbusJwtDecoder가 전역 필터 체인에서
	// 수행하므로 여기에 검증 메서드를 두지 않는다 (두 곳에 두면 규칙이 갈라진다).
	override fun issueAccessToken(userId: UUID): String {
		val now = clock.instant()
		val claims = JWTClaimsSet.Builder()
			.subject(userId.toString())
			.issuer(issuer)
			.issueTime(Date.from(now))
			.expirationTime(Date.from(now.plus(accessTokenTtl)))
			.jwtID(UUID.randomUUID().toString())
			.build()
		val jwt = SignedJWT(JWSHeader(JWSAlgorithm.HS256), claims)
		jwt.sign(signer)
		return jwt.serialize()
	}
}
