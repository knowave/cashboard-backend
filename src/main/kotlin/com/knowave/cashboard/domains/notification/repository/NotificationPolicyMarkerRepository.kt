package com.knowave.cashboard.domains.notification.repository

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.Timestamp
import java.time.Instant
import java.util.UUID

interface NotificationPolicyMarkerRepository {
	fun claimAll(policyKeys: Set<String>, processedAt: Instant): Set<String>
}

@Repository
class NotificationPolicyMarkerRepositoryImpl(
	private val jdbcTemplate: NamedParameterJdbcTemplate,
) : NotificationPolicyMarkerRepository {
	override fun claimAll(policyKeys: Set<String>, processedAt: Instant): Set<String> = policyKeys.mapNotNullTo(linkedSetOf()) { policyKey ->
		jdbcTemplate.query(
			"""
				INSERT INTO notification_policy_markers(id, policy_key, processed_at, created_at, updated_at)
				VALUES (:id, :policyKey, :processedAt, LOCALTIMESTAMP, LOCALTIMESTAMP)
				ON CONFLICT (policy_key) DO NOTHING
				RETURNING policy_key
			""".trimIndent(),
			mapOf("id" to UUID.randomUUID(), "policyKey" to policyKey, "processedAt" to Timestamp.from(processedAt)),
		) { resultSet, _ -> resultSet.getString("policy_key") }.firstOrNull()
	}
}
