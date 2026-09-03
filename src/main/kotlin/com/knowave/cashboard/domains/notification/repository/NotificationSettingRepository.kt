package com.knowave.cashboard.domains.notification.repository

import com.knowave.cashboard.domains.notification.entity.NotificationType
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.util.UUID

interface NotificationSettingRepository {
	fun isEnabled(type: NotificationType, defaultValue: Boolean = true): Boolean
	fun upsert(type: NotificationType, enabled: Boolean)
	fun findAll(): Map<NotificationType, Boolean>
}

@Repository
class NotificationSettingRepositoryImpl(
	private val jdbcTemplate: NamedParameterJdbcTemplate,
) : NotificationSettingRepository {
	override fun isEnabled(type: NotificationType, defaultValue: Boolean): Boolean = jdbcTemplate.query(
		"SELECT enabled FROM notification_settings WHERE type = :type",
		mapOf("type" to type.name),
	) { resultSet, _ -> resultSet.getBoolean("enabled") }.firstOrNull() ?: defaultValue

	override fun upsert(type: NotificationType, enabled: Boolean) {
		jdbcTemplate.update(
			"""
				INSERT INTO notification_settings(id, type, enabled, created_at, updated_at)
				VALUES (:id, :type, :enabled, LOCALTIMESTAMP, LOCALTIMESTAMP)
				ON CONFLICT (type) DO UPDATE SET enabled = EXCLUDED.enabled, updated_at = LOCALTIMESTAMP
			""".trimIndent(),
			mapOf("id" to UUID.randomUUID(), "type" to type.name, "enabled" to enabled),
		)
	}

	override fun findAll(): Map<NotificationType, Boolean> = jdbcTemplate.query(
		"SELECT type, enabled FROM notification_settings",
		emptyMap<String, Any>(),
	) { resultSet, _ -> NotificationType.from(resultSet.getString("type")) to resultSet.getBoolean("enabled") }.toMap()
}
