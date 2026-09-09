package com.knowave.cashboard.domains.user.entity

import com.knowave.cashboard.common.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "refresh_tokens")
class RefreshToken(
	@Column(name = "user_id", nullable = false, updatable = false)
	val userId: UUID,

	@Column(name = "token_hash", nullable = false, unique = true, updatable = false, length = 64)
	val tokenHash: String,

	@Column(name = "expires_at", nullable = false, updatable = false)
	val expiresAt: Instant,

	@Column(name = "revoked_at")
	var revokedAt: Instant? = null,
) : BaseEntity()
