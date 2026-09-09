package com.knowave.cashboard.domains.notification.repository

import com.knowave.cashboard.support.PostgreSqlIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import java.time.LocalDate
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class BalanceShortageStateRepositoryIntegrationTest : PostgreSqlIntegrationTest() {
	@Autowired lateinit var repository: BalanceShortageStateRepository
	@Autowired lateinit var jdbcTemplate: JdbcTemplate

	private lateinit var userId: UUID

	@BeforeEach
	fun clearState() {
		jdbcTemplate.execute("TRUNCATE TABLE balance_shortage_states")
		userId = persistUser().id
	}

	// ponytail: V8 미실행 대기 — balance_shortage_states는 아직 UNIQUE(scope_key)만 갖고
	// UNIQUE(user_id)가 없다(V8 항목 35-f). BalanceShortageStateJpaRepository.insertInitialIfAbsent의
	// `ON CONFLICT (user_id)`는 매칭되는 제약이 없어 Postgres가 계획 단계에서 즉시
	// "there is no unique or exclusion constraint matching the ON CONFLICT specification"로
	// 거부한다 — 동시성 여부와 무관하게 매 호출이 실패한다. V8이 UNIQUE(user_id)를 만들면 통과한다.
	@Disabled("V8__enforce_user_ownership.sql(Stage 3.5) 적용 후 활성화. ON CONFLICT (user_id, ...) 대상 제약이 아직 없다.")
	@Test
	fun `빈 상태에서 같은 사용자 전이는 동시에도 episode 하나와 신규 전이 하나만 만든다`() {
		val ready = CountDownLatch(2)
		val start = CountDownLatch(1)
		val shortageDate = LocalDate.of(2026, 9, 24)

		val transitions = Executors.newFixedThreadPool(2).use { executor ->
			val futures = (1..2).map {
				executor.submit<BalanceShortageTransition> {
					ready.countDown()
					check(start.await(5, TimeUnit.SECONDS))
					repository.transition(userId, shortageDate)
				}
			}
			check(ready.await(5, TimeUnit.SECONDS))
			start.countDown()
			futures.map { it.get(10, TimeUnit.SECONDS) }
		}

		assertThat(transitions.map(BalanceShortageTransition::episode)).containsOnly(1L)
		assertThat(transitions.count(BalanceShortageTransition::newEpisode)).isEqualTo(1)
		assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM balance_shortage_states", Long::class.java)).isEqualTo(1L)
		assertThat(
			jdbcTemplate.queryForObject(
				"SELECT episode FROM balance_shortage_states WHERE user_id = ?",
				Long::class.java,
				userId,
			),
		).isEqualTo(1L)
	}
}
