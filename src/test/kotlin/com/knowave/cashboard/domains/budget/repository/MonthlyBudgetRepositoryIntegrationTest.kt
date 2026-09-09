package com.knowave.cashboard.domains.budget.repository

import com.knowave.cashboard.domains.budget.entity.MonthlyBudget
import com.knowave.cashboard.support.PostgreSqlIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
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

	private fun insertBudget(id: UUID, userId: UUID, targetMonth: String) {
		jdbcTemplate.update(
			"""
				INSERT INTO monthly_budgets (id, user_id, target_month, monthly_budget, used_amount, created_at, updated_at)
				VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
			""".trimIndent(),
			id,
			userId,
			targetMonth,
			100L,
			70L,
		)
	}

	@Test
	fun `두 번째 트랜잭션은 첫 번째 트랜잭션이 끝나기 전에는 월 예산 행 잠금을 얻지 못한다`() {
		val user = persistUser()
		val budgetId = UUID.randomUUID()
		insertBudget(budgetId, user.id, "2026-09")
		val firstLockAcquired = CountDownLatch(1)
		val releaseFirstTransaction = CountDownLatch(1)
		val secondTransactionEntered = CountDownLatch(1)
		val secondLockAcquired = CountDownLatch(1)
		val executor = Executors.newFixedThreadPool(2)

		val first = executor.submit {
			transactionTemplate.executeWithoutResult {
				requireNotNull(monthlyBudgetRepository.findByIdForUpdate(budgetId, user.id))
				firstLockAcquired.countDown()
				check(releaseFirstTransaction.await(10, TimeUnit.SECONDS))
			}
		}
		try {
			check(firstLockAcquired.await(10, TimeUnit.SECONDS))
			val second = executor.submit {
				transactionTemplate.executeWithoutResult {
					secondTransactionEntered.countDown()
					requireNotNull(monthlyBudgetRepository.findByIdForUpdate(budgetId, user.id))
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

	@Test
	fun `A가 B의 예산 id로 존재 확인을 통과하지 못한다`() {
		val userA = persistUser()
		val userB = persistUser()
		val budgetId = UUID.randomUUID()
		insertBudget(budgetId, userB.id, "2026-09")

		assertThat(monthlyBudgetRepository.existsByIdAndUserId(budgetId, userA.id)).isFalse()
		assertThat(monthlyBudgetRepository.existsByIdAndUserId(budgetId, userB.id)).isTrue()
	}

	// AC-20d: 최종 상태(V8 이후 복합 UNIQUE(user_id, target_month)) 기준의 의도를 코드로 남긴다.
	@Disabled(
		"V8__enforce_user_ownership.sql(Stage 3.5) 적용 후 활성화. " +
			"현재는 글로벌 UNIQUE(target_month)가 살아 있어 두 사용자가 같은 월을 가질 수 없다.",
	)
	@Test
	fun `사용자 A와 B는 같은 target_month로 각각 예산을 생성할 수 있다`() {
		val userA = persistUser()
		val userB = persistUser()
		monthlyBudgetRepository.save(MonthlyBudget(userA.id, "2026-09", 100, 0))

		assertThat(monthlyBudgetRepository.existsByTargetMonthAndUserId("2026-09", userB.id)).isFalse()

		monthlyBudgetRepository.save(MonthlyBudget(userB.id, "2026-09", 100, 0))
	}
}
