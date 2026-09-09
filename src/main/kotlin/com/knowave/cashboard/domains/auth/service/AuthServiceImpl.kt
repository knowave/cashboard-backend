package com.knowave.cashboard.domains.auth.service

import com.knowave.cashboard.common.exception.InvalidRefreshTokenException
import com.knowave.cashboard.common.exception.InvalidTimezoneException
import com.knowave.cashboard.common.exception.UnsupportedSocialProviderException
import com.knowave.cashboard.domains.auth.policy.SocialLoginPolicy
import com.knowave.cashboard.domains.auth.security.JwtTokenProvider
import com.knowave.cashboard.domains.auth.service.dto.SocialLoginCommand
import com.knowave.cashboard.domains.auth.service.dto.TokenPairResult
import com.knowave.cashboard.domains.auth.social.SocialAuthProvider
import com.knowave.cashboard.domains.user.service.RefreshTokenService
import com.knowave.cashboard.domains.user.service.UserService
import com.knowave.cashboard.domains.user.service.dto.GetOrCreateUserCommand
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.convert.DurationStyle
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom
import java.time.Clock
import java.time.Duration
import java.time.ZoneId
import java.util.Base64
import java.util.UUID

@Service
class AuthServiceImpl(
	private val socialAuthProviders: List<SocialAuthProvider>,
	private val socialLoginPolicy: SocialLoginPolicy,
	private val userService: UserService,
	private val refreshTokenService: RefreshTokenService,
	private val jwtTokenProvider: JwtTokenProvider,
	private val clock: Clock,
	@Value("\${auth.jwt.refresh-token-ttl}") refreshTokenTtlText: String,
) : AuthService {
	private val secureRandom = SecureRandom()
	// JwtTokenProviderImpl과 동일하게 String으로 받아 DurationStyle로 직접 파싱한다
	// (Spring @Value의 자동 Duration 변환에 기대지 않는다).
	private val refreshTokenTtl: Duration = DurationStyle.detectAndParse(refreshTokenTtlText)

	// ponytail: @Transactional을 붙이지 않는다. authenticate()가 Provider 서버로 나가는 외부 HTTP라
	// 트랜잭션에 감싸면 Provider 지연 동안 DB 커넥션을 잡아 풀을 고갈시킨다.
	// User 생성은 userService.getOrCreate()가 자체 @Transactional로 원자성을 보장하고,
	// RefreshToken 삽입은 단일 INSERT라 그 자체로 원자적이다.
	override fun socialLogin(command: SocialLoginCommand): TokenPairResult {
		// Provider 호출(외부 네트워크 요청) 전에 플랫폼×Provider 허용 여부부터 거부한다.
		socialLoginPolicy.validate(command.platform, command.provider)
		runCatching { ZoneId.of(command.timezone) }
			.getOrElse { throw InvalidTimezoneException(command.timezone) }

		val socialAuthProvider = socialAuthProviders.firstOrNull { it.supports(command.provider) }
			?: throw UnsupportedSocialProviderException(command.provider.name)
		val socialUserInfo = socialAuthProvider.authenticate(
			command.authorizationCode,
			command.pkceCodeVerifier,
			command.platform,
		)

		val user = userService.getOrCreate(
			GetOrCreateUserCommand(
				providerName = command.provider.name,
				providerId = socialUserInfo.providerId,
				email = socialUserInfo.email,
				providerNickname = socialUserInfo.nickname,
				profileImageUrl = socialUserInfo.profileImageUrl,
				timezone = command.timezone,
			),
		)
		return issueTokenPair(user.id)
	}

	@Transactional
	override fun refresh(refreshToken: String): TokenPairResult {
		// 회전 실패 사유(만료·미존재·재사용)는 refreshTokenService가 판정하고 필요한 폐기까지 끝낸다.
		// 여기서는 재로그인을 요구하는 것 외에 할 일이 없다.
		val userId = refreshTokenService.rotate(refreshToken, clock.instant())
			?: throw InvalidRefreshTokenException()
		return issueTokenPair(userId)
	}

	override fun logout(refreshToken: String, logoutAllDevices: Boolean) {
		refreshTokenService.revoke(refreshToken, clock.instant(), logoutAllDevices)
	}

	private fun issueTokenPair(userId: UUID): TokenPairResult {
		val accessToken = jwtTokenProvider.issueAccessToken(userId)
		val plainRefreshToken = generatePlainRefreshToken()
		refreshTokenService.issue(userId, plainRefreshToken, clock.instant().plus(refreshTokenTtl))
		return TokenPairResult(accessToken = accessToken, refreshToken = plainRefreshToken)
	}

	// 256비트(32바이트) SecureRandom 문자열. URL-safe base64로 인코딩해 그대로 응답 본문에 담는다.
	// 해싱은 refreshTokenService가 저장 시점에 한다 — auth는 해시 방식을 모른다.
	private fun generatePlainRefreshToken(): String {
		val bytes = ByteArray(32)
		secureRandom.nextBytes(bytes)
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
	}
}
