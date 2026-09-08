package com.knowave.cashboard.domains.notification.repository

import com.knowave.cashboard.support.PostgreSqlIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import java.time.LocalDate
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class BalanceShortageStateRepositoryIntegrationTest : PostgreSqlIntegrationTest() {
	@Autowired lateinit var repository: BalanceShortageStateRepository
	@Autowired lateinit var jdbcTemplate: JdbcTemplate

	@BeforeEach
	fun clearState() {
		jdbcTemplate.execute("TRUNCATE TABLE balance_shortage_states")
	}

	@Test
	fun `빈 상태에서 같은 scope 전이는 동시에도 episode 하나와 신규 전이 하나만 만든다`() {
		val ready = CountDownLatch(2)
		val start = CountDownLatch(1)
		val shortageDate = LocalDate.of(2026, 9, 24)

		val transitions = Executors.newFixedThreadPool(2).use { executor ->
			val futures = (1..2).map {
				executor.submit<BalanceShortageTransition> {
					ready.countDown()
					check(start.await(5, TimeUnit.SECONDS))
					repository.transition("SINGLE_USER", shortageDate)
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
				"SELECT episode FROM balance_shortage_states WHERE scope_key = ?",
				Long::class.java,
				"SINGLE_USER",
			),
		).isEqualTo(1L)
	}
}
