package com.knowave.cashboard.domains.user.repository

import com.knowave.cashboard.domains.user.entity.User
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class UserRepositoryImpl(
	private val userJpaRepository: UserJpaRepository,
	private val jdbcTemplate: NamedParameterJdbcTemplate,
) : UserRepository {
	override fun findByProviderAndProviderId(providerName: String, providerId: String): User? =
		userJpaRepository.findByProviderAndProviderId(providerName, providerId)

	// 동시 최초 로그인에서 같은 (provider, provider_id)로 두 요청이 들어와도 행이 1건만 생기도록
	// ON CONFLICT DO NOTHING으로 흡수한다. 호출부는 결과와 무관하게 findByProviderAndProviderId로
	// 재조회해 최종적으로 존재하는 행을 가져간다.
	override fun insertIfAbsent(user: User): Boolean = jdbcTemplate.update(
		"""
			INSERT INTO users(id, provider, provider_id, email, nickname, profile_image_url, timezone, created_at, updated_at)
			VALUES (:id, :provider, :providerId, :email, :nickname, :profileImageUrl, :timezone, LOCALTIMESTAMP, LOCALTIMESTAMP)
			ON CONFLICT (provider, provider_id) DO NOTHING
		""".trimIndent(),
		mapOf(
			"id" to UUID.randomUUID(),
			"provider" to user.provider,
			"providerId" to user.providerId,
			"email" to user.email,
			"nickname" to user.nickname,
			"profileImageUrl" to user.profileImageUrl,
			"timezone" to user.timezone,
		),
	) == 1

	override fun existsByNickname(nickname: String): Boolean = userJpaRepository.existsByNickname(nickname)

	override fun save(user: User): User = userJpaRepository.save(user)

	override fun findById(id: UUID): User? = userJpaRepository.findById(id).orElse(null)
}
