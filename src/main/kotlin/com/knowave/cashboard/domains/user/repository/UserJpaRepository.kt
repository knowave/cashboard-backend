package com.knowave.cashboard.domains.user.repository

import com.knowave.cashboard.domains.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserJpaRepository : JpaRepository<User, UUID> {
	fun findByProviderAndProviderId(providerName: String, providerId: String): User?
	fun existsByNickname(nickname: String): Boolean
}
