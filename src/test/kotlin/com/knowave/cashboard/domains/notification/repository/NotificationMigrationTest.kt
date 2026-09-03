package com.knowave.cashboard.domains.notification.repository

import com.knowave.cashboard.support.PostgreSqlIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate

class NotificationMigrationTest : PostgreSqlIntegrationTest() {
	@Autowired
	lateinit var jdbcTemplate: JdbcTemplate

	@Test
	fun `알림 enum 컬럼은 varchar이고 멱등 키는 unique다`() {
		assertThat(columnType("notifications", "type")).isEqualTo("character varying")
		assertThat(columnType("notifications", "status")).isEqualTo("character varying")
		assertThat(
			jdbcTemplate.queryForObject(
				"""select count(*) from pg_indexes where tablename='notifications' and indexdef like '%deduplication_key%' and indexdef like '%UNIQUE%'""",
				Int::class.java,
			),
		).isEqualTo(1)
	}

	@Test
	fun `알림 영속화에 필요한 테이블과 인덱스가 생성된다`() {
		val tableCount = jdbcTemplate.queryForObject(
			"""
				SELECT count(*) FROM information_schema.tables
				WHERE table_schema = 'public'
				AND table_name IN (
					'notifications',
					'notification_settings',
					'notification_preferences',
					'balance_shortage_states',
					'notification_policy_markers'
				)
			""".trimIndent(),
			Int::class.java,
		)

		assertThat(tableCount).isEqualTo(5)
		assertThat(indexExists("idx_notifications_feed")).isTrue()
		assertThat(indexExists("idx_notifications_unread")).isTrue()
		assertThat(
			jdbcTemplate.queryForObject(
				"""select count(*) from pg_indexes where tablename='notification_policy_markers' and indexdef like '%policy_key%' and indexdef like '%UNIQUE%'""",
				Int::class.java,
			),
		).isEqualTo(1)
	}

	private fun columnType(tableName: String, columnName: String): String = jdbcTemplate.queryForObject(
		"""select data_type from information_schema.columns where table_name = ? and column_name = ?""",
		String::class.java,
		tableName,
		columnName,
	)!!

	private fun indexExists(indexName: String): Boolean = jdbcTemplate.queryForObject(
		"SELECT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = ?)",
		Boolean::class.java,
		indexName,
	)!!
}
