package com.knowave.cashboard.support

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.postgresql.PostgreSQLContainer

@SpringBootTest
abstract class PostgreSqlIntegrationTest {
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
		}
	}
}
