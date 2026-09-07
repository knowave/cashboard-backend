package com.knowave.cashboard.domains.budget.repository

import com.knowave.cashboard.support.PostgreSqlIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.support.TransactionTemplate
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MonthlyBudgetRepositoryIntegrationTest : PostgreSqlIntegrationTest() {
	@Autowired lateinit var monthlyBudgetRepository: MonthlyBudgetRepository
	@Autowired lateinit var transactionTemplate: TransactionTemplate
	@Autowired lateinit var jdbcTemplate: JdbcTemplate

	@BeforeEach
	fun isolateDatabase() {
		jdbcTemplate.execute("TRUNCATE TABLE monthly_budgets CASCADE")
	}

	@Test
	fun `두 번째 트랜잭션은 첫 번째 트랜잭션이 끝나기 전에는 월 예산 행 잠금을 얻지 못한다`() {
		val budgetId = UUID.randomUUID()
		jdbcTemplate.update(
			"""
				INSERT INTO monthly_budgets (id, target_month, monthly_budget, used_amount, created_at, updated_at)
				VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
			""".trimIndent(),
			budgetId,
			"2026-09",
			100L,
			70L,
		)
		val firstLockAcquired = CountDownLatch(1)
		val releaseFirstTransaction = CountDownLatch(1)
		val secondTransactionEntered = CountDownLatch(1)
		val secondLockAcquired = CountDownLatch(1)
		val executor = Executors.newFixedThreadPool(2)

		val first = executor.submit {
			transactionTemplate.executeWithoutResult {
				requireNotNull(monthlyBudgetRepository.findByIdForUpdate(budgetId))
				firstLockAcquired.countDown()
				check(releaseFirstTransaction.await(10, TimeUnit.SECONDS))
			}
		}
		try {
			check(firstLockAcquired.await(10, TimeUnit.SECONDS))
			val second = executor.submit {
				transactionTemplate.executeWithoutResult {
					secondTransactionEntered.countDown()
					requireNotNull(monthlyBudgetRepository.findByIdForUpdate(budgetId))
					secondLockAcquired.countDown()
				}
			}

			check(secondTransactionEntered.await(10, TimeUnit.SECONDS))
			assertThat(secondLockAcquired.await(1, TimeUnit.SECONDS)).isFalse()

			releaseFirstTransaction.countDown()
			first.get(10, TimeUnit.SECONDS)
			second.get(10, TimeUnit.SECONDS)
		} finally {
			releaseFirstTransaction.countDown()
			executor.shutdownNow()
			check(executor.awaitTermination(10, TimeUnit.SECONDS))
		}
	}
}
