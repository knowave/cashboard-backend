package com.knowave.cashboard.support

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.postgresql.PostgreSQLContainer
import java.time.LocalDateTime
import java.util.UUID

// ponytail: Stage 2(User 엔티티/Repository)는 이번 범위 밖이다. V6가 만든 `users` 테이블에
// JDBC로 직접 행을 심어 FK를 만족시키는 최소 fixture. Stage 2가 들어오면 UserRepository로 교체.
data class User(val id: UUID, val timezone: String)

@SpringBootTest
abstract class PostgreSqlIntegrationTest {
	@Autowired
	private lateinit var jdbcTemplate: JdbcTemplate

	// ponytail: provider/provider_id는 V6에서 NOT NULL이고 (provider, provider_id) UNIQUE다.
	// id를 provider_id로 재사용하면 테스트마다 유일성이 보장된다. nickname도 UNIQUE라 id를 섞는다.
	fun persistUser(
		timezone: String = "Asia/Seoul",
		id: UUID = UUID.randomUUID(),
		provider: String = "GOOGLE",
		providerId: String = id.toString(),
		email: String? = null,
	): User {
		val now = LocalDateTime.now()
		jdbcTemplate.update(
			"INSERT INTO users(id, provider, provider_id, email, nickname, timezone, created_at, updated_at) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
			id, provider, providerId, email, "tester-$id", timezone, now, now,
		)
		return User(id, timezone)
	}

	companion object {
		@JvmStatic
		val postgres = PostgreSQLContainer("postgres:17-alpine").apply { start() }

		@DynamicPropertySource
		@JvmStatic
		fun datasource(registry: DynamicPropertyRegistry) {
			registry.add("spring.datasource.url", postgres::getJdbcUrl)
			registry.add("spring.datasource.username", postgres::getUsername)
			registry.add("spring.datasource.password", postgres::getPassword)
			// 기존 도메인의 누락 마이그레이션이 알림 Flyway 계약 검증을 막지 않도록 JPA 자동 DDL을 끈다.
			registry.add("spring.jpa.hibernate.ddl-auto") { "none" }
			// ponytail: JwtTokenProviderImpl이 @PostConstruct에서 32바이트 미만 시크릿을 부팅 실패로 막는다.
			// .env가 비어 있으면 모든 @SpringBootTest 컨텍스트가 뜨지 않으므로 테스트 전용 값을 준다.
			registry.add("auth.jwt.secret") { "test-secret-key-for-integration-tests-32b+" }
		}
	}
}
