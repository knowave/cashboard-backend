package com.knowave.cashboard.domains.user.service

import com.knowave.cashboard.domains.user.repository.RefreshTokenRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.Instant
import java.util.HexFormat
import java.util.UUID

@Service
class RefreshTokenServiceImpl(
	private val refreshTokenRepository: RefreshTokenRepository,
) : RefreshTokenService {
	private val logger = LoggerFactory.getLogger(javaClass)

	override fun issue(userId: UUID, refreshToken: String, expiresAt: Instant) {
		refreshTokenRepository.save(userId, sha256Hex(refreshToken), expiresAt)
	}

	@Transactional
	override fun rotate(refreshToken: String, now: Instant): UUID? {
		val tokenHash = sha256Hex(refreshToken)
		refreshTokenRepository.revokeIfActive(tokenHash, now)?.let { return it }

		// 원자적 UPDATE가 0행이었는데도 해시가 이미 존재한다면 = 이미 폐기된(또는 만료된) 토큰의
		// 재사용 시도다. OAuth 2.0 Security BCP에 따라 도난 재사용을 조기 차단하기 위해
		// 해당 User의 RefreshToken을 전부 폐기하고 재로그인을 요구한다.
		if (refreshTokenRepository.existsByTokenHash(tokenHash)) {
			refreshTokenRepository.findUserIdByTokenHash(tokenHash)?.let { stolenUserId ->
				logger.warn("Refresh token reuse detected. Revoking all sessions. userId={}", stolenUserId)
				refreshTokenRepository.deleteAllByUserId(stolenUserId)
			}
		}
		return null
	}

	@Transactional
	override fun revoke(refreshToken: String, now: Instant, logoutAllDevices: Boolean) {
		val tokenHash = sha256Hex(refreshToken)
		if (logoutAllDevices) {
			refreshTokenRepository.findUserIdByTokenHash(tokenHash)?.let { userId ->
				refreshTokenRepository.deleteAllByUserId(userId)
			}
		} else {
			refreshTokenRepository.revokeIfActive(tokenHash, now)
		}
	}

	// SHA-256, salt 없는 결정적 해시. token_hash로 인덱스 조회를 해야 하고 원문이 이미
	// 고엔트로피(SecureRandom 256비트)라 bcrypt/argon2 같은 완속 해시의 방어 이점이 없다.
	private fun sha256Hex(refreshToken: String): String =
		HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(refreshToken.toByteArray(Charsets.UTF_8)))
}
