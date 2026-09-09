package com.knowave.cashboard.domains.notification.repository

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.Timestamp
import java.time.Instant
import java.util.UUID

interface NotificationPolicyMarkerRepository {
	fun claimAll(userId: UUID, policyKeys: Set<String>, processedAt: Instant): Set<String>
}

@Repository
class NotificationPolicyMarkerRepositoryImpl(
	private val jdbcTemplate: NamedParameterJdbcTemplate,
) : NotificationPolicyMarkerRepository {
	// ponytail: ON CONFLICT (user_id, policy_key) requires the composite UNIQUE that V8
	// (Stage 3.5, not yet run) creates. Until V8 runs, the global UNIQUE(policy_key) still
	// governs and a second user's claim on an already-claimed key raises a duplicate-key
	// error here instead of being silently skipped. Expected pre-V8 breakage.
	override fun claimAll(userId: UUID, policyKeys: Set<String>, processedAt: Instant): Set<String> = policyKeys.mapNotNullTo(linkedSetOf()) { policyKey ->
		jdbcTemplate.query(
			"""
				INSERT INTO notification_policy_markers(id, user_id, policy_key, processed_at, created_at, updated_at)
				VALUES (:id, :userId, :policyKey, :processedAt, LOCALTIMESTAMP, LOCALTIMESTAMP)
				ON CONFLICT (user_id, policy_key) DO NOTHING
				RETURNING policy_key
			""".trimIndent(),
			mapOf("id" to UUID.randomUUID(), "userId" to userId, "policyKey" to policyKey, "processedAt" to Timestamp.from(processedAt)),
		) { resultSet, _ -> resultSet.getString("policy_key") }.firstOrNull()
	}
}
