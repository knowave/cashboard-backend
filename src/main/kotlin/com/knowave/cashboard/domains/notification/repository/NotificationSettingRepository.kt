package com.knowave.cashboard.domains.notification.repository

import com.knowave.cashboard.domains.notification.entity.NotificationType
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.util.UUID

interface NotificationSettingRepository {
	fun isEnabled(userId: UUID, type: NotificationType, defaultValue: Boolean = true): Boolean
	fun upsert(userId: UUID, type: NotificationType, enabled: Boolean)
	fun findAll(userId: UUID): Map<NotificationType, Boolean>
}

@Repository
class NotificationSettingRepositoryImpl(
	private val jdbcTemplate: NamedParameterJdbcTemplate,
) : NotificationSettingRepository {
	override fun isEnabled(userId: UUID, type: NotificationType, defaultValue: Boolean): Boolean = jdbcTemplate.query(
		"SELECT enabled FROM notification_settings WHERE user_id = :userId AND type = :type",
		mapOf("userId" to userId, "type" to type.name),
	) { resultSet, _ -> resultSet.getBoolean("enabled") }.firstOrNull() ?: defaultValue

	// ponytail: ON CONFLICT (user_id, type) requires the composite UNIQUE that V8 (Stage 3.5,
	// not yet run) creates. Until V8 runs, the global UNIQUE(type) still governs and a second
	// user's upsert on an already-used type raises a duplicate-key error instead of merging.
	// Expected pre-V8 breakage.
	override fun upsert(userId: UUID, type: NotificationType, enabled: Boolean) {
		jdbcTemplate.update(
			"""
				INSERT INTO notification_settings(id, user_id, type, enabled, created_at, updated_at)
				VALUES (:id, :userId, :type, :enabled, LOCALTIMESTAMP, LOCALTIMESTAMP)
				ON CONFLICT (user_id, type) DO UPDATE SET enabled = EXCLUDED.enabled, updated_at = LOCALTIMESTAMP
			""".trimIndent(),
			mapOf("id" to UUID.randomUUID(), "userId" to userId, "type" to type.name, "enabled" to enabled),
		)
	}

	override fun findAll(userId: UUID): Map<NotificationType, Boolean> = jdbcTemplate.query(
		"SELECT type, enabled FROM notification_settings WHERE user_id = :userId",
		mapOf("userId" to userId),
	) { resultSet, _ -> NotificationType.from(resultSet.getString("type")) to resultSet.getBoolean("enabled") }.toMap()
}
