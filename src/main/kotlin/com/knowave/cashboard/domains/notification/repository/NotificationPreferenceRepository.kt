package com.knowave.cashboard.domains.notification.repository

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.util.UUID

interface NotificationPreferenceRepository {
	fun getPushEnabled(defaultValue: Boolean = true): Boolean
	fun upsertPushEnabled(enabled: Boolean)
}

@Repository
class NotificationPreferenceRepositoryImpl(
	private val jdbcTemplate: NamedParameterJdbcTemplate,
) : NotificationPreferenceRepository {
	override fun getPushEnabled(defaultValue: Boolean): Boolean = jdbcTemplate.query(
		"SELECT push_enabled FROM notification_preferences WHERE scope_key = 'SINGLE_USER'",
		emptyMap<String, Any>(),
	) { resultSet, _ -> resultSet.getBoolean("push_enabled") }.firstOrNull() ?: defaultValue

	override fun upsertPushEnabled(enabled: Boolean) {
		jdbcTemplate.update(
			"""
				INSERT INTO notification_preferences(id, scope_key, push_enabled, created_at, updated_at)
				VALUES (:id, 'SINGLE_USER', :enabled, LOCALTIMESTAMP, LOCALTIMESTAMP)
				ON CONFLICT (scope_key) DO UPDATE SET push_enabled = EXCLUDED.push_enabled, updated_at = LOCALTIMESTAMP
			""".trimIndent(),
			mapOf("id" to UUID.randomUUID(), "enabled" to enabled),
		)
	}
}
