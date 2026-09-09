package com.knowave.cashboard.domains.expenseanalysis.service

import com.knowave.cashboard.support.PostgreSqlIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

// 계획 시나리오 1(최고위험): 집계 SQL이 user_id로 스코프되지 않으면 합계 숫자에만 타 사용자
// 금액이 섞여 들어가 눈에 띄지 않는다. ExpenseAnalysisJpaRepository의 @Query 2개에 넣은
// WHERE user_id = :userId가 실제 DB에서 동작하는지 검증한다 (mock 기반 단위 테스트로는
// SQL 자체를 검증할 수 없다).
// ponytail: budget_expenses를 BudgetStrategyService가 아니라 JDBC로 직접 심는다.
// 서비스를 거치면 BudgetUsageChangedEvent가 발행되고, 그 알림 리스너가 호출하는
// notification_policy_markers.claimAll의 ON CONFLICT (user_id, policy_key)는 V8이
// 만드는 복합 UNIQUE가 있어야 성립한다 — 이 테스트의 관심사(집계 격리)와 무관한 이유로
// V8 이전엔 항상 실패한다.
class ExpenseAnalysisServiceIsolationIntegrationTest : PostgreSqlIntegrationTest() {
	@Autowired lateinit var expenseAnalysisService: ExpenseAnalysisService
	@Autowired lateinit var jdbcTemplate: JdbcTemplate

	@BeforeEach
	fun isolateDatabase() {
		jdbcTemplate.execute("TRUNCATE TABLE budget_expenses, monthly_budgets, users CASCADE")
	}

	@Test
	fun `다른 사용자의 지출 금액은 내 월별 집계에 섞이지 않는다`() {
		val userA = persistUser()
		val userB = persistUser()

		val budgetA = persistMonthlyBudget(userA.id, targetMonth = "2026-08")
		val budgetB = persistMonthlyBudget(userB.id, targetMonth = "2026-09")
		persistBudgetExpense(userA.id, budgetA, amount = 100_000L, spentAt = LocalDate.of(2026, 8, 10))
		persistBudgetExpense(userB.id, budgetB, amount = 999_999_999L, spentAt = LocalDate.of(2026, 8, 15))

		val result = expenseAnalysisService.getAnalysis(userA.id, 2026, 8)

		assertThat(result.totalExpense).isEqualTo(100_000L)
		assertThat(result.categories).hasSize(1)
		assertThat(result.categories.single().amount).isEqualTo(100_000L)
	}

	private fun persistMonthlyBudget(userId: UUID, targetMonth: String): UUID {
		val id = UUID.randomUUID()
		val now = LocalDateTime.now()
		jdbcTemplate.update(
			"""
				INSERT INTO monthly_budgets(id, user_id, target_month, monthly_budget, used_amount, created_at, updated_at)
				VALUES (?, ?, ?, ?, ?, ?, ?)
			""".trimIndent(),
			id, userId, targetMonth, 1_000_000L, 0L, now, now,
		)
		return id
	}

	private fun persistBudgetExpense(userId: UUID, monthlyBudgetId: UUID, amount: Long, spentAt: LocalDate) {
		val now = LocalDateTime.now()
		jdbcTemplate.update(
			"""
				INSERT INTO budget_expenses(id, user_id, monthly_budget_id, amount, category, spent_at, created_at, updated_at)
				VALUES (?, ?, ?, ?, ?, ?, ?, ?)
			""".trimIndent(),
			UUID.randomUUID(), userId, monthlyBudgetId, amount, "food", spentAt, now, now,
		)
	}
}
