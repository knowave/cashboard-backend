package com.knowave.cashboard.domains.user.repository

import com.knowave.cashboard.domains.user.entity.RefreshToken
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import java.time.Instant
import java.util.UUID

@Repository
class RefreshTokenRepositoryImpl(
	private val refreshTokenJpaRepository: RefreshTokenJpaRepository,
	private val jdbcTemplate: NamedParameterJdbcTemplate,
) : RefreshTokenRepository {
	override fun save(userId: UUID, tokenHash: String, expiresAt: Instant) {
		refreshTokenJpaRepository.save(
			RefreshToken(userId = userId, tokenHash = tokenHash, expiresAt = expiresAt),
		)
	}

	// UPDATE ... RETURNING을 SELECT류로 실행해 갱신 성공 여부와 user_id를 한 번에 얻는다.
	// (AccountBalanceLockRepositoryImpl의 pg_advisory_xact_lock과 같은 이유로 .update()가 아닌
	// .query()를 쓴다 — RETURNING이 붙은 문장은 결과 집합을 내놓는다.)
	override fun revokeIfActive(tokenHash: String, now: Instant): UUID? = jdbcTemplate.query(
		"""
			UPDATE refresh_tokens SET revoked_at = :now, updated_at = :now
			WHERE token_hash = :tokenHash AND revoked_at IS NULL AND expires_at > :now
			RETURNING user_id
		""".trimIndent(),
		mapOf("tokenHash" to tokenHash, "now" to Timestamp.from(now)),
	) { resultSet, _ -> UUID.fromString(resultSet.getString("user_id")) }.firstOrNull()

	@Transactional
	override fun deleteAllByUserId(userId: UUID) {
		refreshTokenJpaRepository.deleteAllByUserId(userId)
	}

	override fun existsByTokenHash(tokenHash: String): Boolean = refreshTokenJpaRepository.existsByTokenHash(tokenHash)

	override fun findUserIdByTokenHash(tokenHash: String): UUID? = refreshTokenJpaRepository.findByTokenHash(tokenHash)?.userId
}
