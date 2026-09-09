package com.knowave.cashboard.domains.notification.repository

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.util.UUID

interface NotificationPreferenceRepository {
	fun getPushEnabled(userId: UUID, defaultValue: Boolean = true): Boolean
	fun upsertPushEnabled(userId: UUID, enabled: Boolean)
}

@Repository
class NotificationPreferenceRepositoryImpl(
	private val jdbcTemplate: NamedParameterJdbcTemplate,
) : NotificationPreferenceRepository {
	override fun getPushEnabled(userId: UUID, defaultValue: Boolean): Boolean = jdbcTemplate.query(
		"SELECT push_enabled FROM notification_preferences WHERE user_id = :userId",
		mapOf("userId" to userId),
	) { resultSet, _ -> resultSet.getBoolean("push_enabled") }.firstOrNull() ?: defaultValue

	// ponytail: scope_key is still NOT NULL (V8 hasn't dropped it) so it stays hardcoded here.
	// ON CONFLICT targets user_id in anticipation of V8's UNIQUE(user_id); until V8 runs, a
	// second user's upsert can raise a duplicate-key error against the still-global
	// UNIQUE(scope_key) instead of being silently merged. Expected pre-V8 breakage.
	override fun upsertPushEnabled(userId: UUID, enabled: Boolean) {
		jdbcTemplate.update(
			"""
				INSERT INTO notification_preferences(id, user_id, scope_key, push_enabled, created_at, updated_at)
				VALUES (:id, :userId, 'SINGLE_USER', :enabled, LOCALTIMESTAMP, LOCALTIMESTAMP)
				ON CONFLICT (user_id) DO UPDATE SET push_enabled = EXCLUDED.push_enabled, updated_at = LOCALTIMESTAMP
			""".trimIndent(),
			mapOf("id" to UUID.randomUUID(), "userId" to userId, "enabled" to enabled),
		)
	}
}
