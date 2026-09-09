package com.knowave.cashboard.domains.user.repository

import com.knowave.cashboard.domains.user.entity.User
import java.util.UUID

interface UserRepository {
	fun findByProviderAndProviderId(providerName: String, providerId: String): User?
	fun insertIfAbsent(user: User): Boolean
	fun existsByNickname(nickname: String): Boolean
	fun save(user: User): User
	fun findById(id: UUID): User?
}
