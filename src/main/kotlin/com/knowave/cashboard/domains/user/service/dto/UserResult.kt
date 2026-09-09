package com.knowave.cashboard.domains.user.service.dto

import com.knowave.cashboard.common.exception.PersistedEntityIdMissingException
import com.knowave.cashboard.domains.user.entity.User
import java.time.LocalDateTime
import java.util.UUID

data class UserResult(
	val id: UUID,
	val provider: String,
	val providerId: String,
	val email: String?,
	val nickname: String,
	val profileImageUrl: String?,
	val timezone: String,
	val createdAt: LocalDateTime?,
	val updatedAt: LocalDateTime?,
)

fun User.toResult(): UserResult = UserResult(
	id = id ?: throw PersistedEntityIdMissingException("User"),
	provider = provider,
	providerId = providerId,
	email = email,
	nickname = nickname,
	profileImageUrl = profileImageUrl,
	timezone = timezone,
	createdAt = createdAt,
	updatedAt = updatedAt,
)
