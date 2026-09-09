package com.knowave.cashboard.domains.notification

import com.knowave.cashboard.domains.account.entity.AccountType
import com.knowave.cashboard.domains.account.service.AccountService
import com.knowave.cashboard.domains.account.service.dto.CreateAccountCommand
import com.knowave.cashboard.domains.account.service.dto.UpdateAccountCommand
import com.knowave.cashboard.domains.assetgoal.service.AssetGoalService
import com.knowave.cashboard.domains.assetgoal.service.dto.CreateAssetGoalCommand
import com.knowave.cashboard.domains.assetgoal.service.dto.UpdateAssetGoalCommand
import com.knowave.cashboard.domains.budget.event.BudgetUsageChangedEvent
import com.knowave.cashboard.domains.budget.repository.MonthlyBudgetRepository
import com.knowave.cashboard.domains.budget.service.BudgetStrategyService
import com.knowave.cashboard.domains.budget.service.dto.CreateBudgetExpenseCommand
import com.knowave.cashboard.domains.budget.service.dto.CreateMonthlyBudgetCommand
import com.knowave.cashboard.domains.budget.service.dto.UpdateUsedAmountCommand
import com.knowave.cashboard.domains.notification.entity.NotificationType
import com.knowave.cashboard.domains.notification.repository.NewNotification
import com.knowave.cashboard.domains.notification.repository.NotificationSettingRepository
import com.knowave.cashboard.domains.notification.service.NotificationGenerationService
import com.knowave.cashboard.support.PostgreSqlIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.core.JdbcTemplate
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

// ponytail: V8 미실행 대기 — 이 파일의 시나리오는 전부 NotificationPolicyMarkerRepositoryImpl.claimAll
// 또는 NotificationSettingRepositoryImpl.upsert를 거친다. 두 SQL 모두 `ON CONFLICT (user_id, ...)`가
// V8의 복합 UNIQUE를 전제하는데 V8이 아직 실행되지 않아 notification_policy_markers는
// UNIQUE(policy_key)만, notification_settings는 UNIQUE(type)만 갖는다. Postgres는 대상 제약이
// 없는 ON CONFLICT를 계획 단계에서 즉시 거부하므로(동시성·사용자 수와 무관) 이 클래스의 테스트는
// 전부 V8 적용 전까지 실패가 예상된다.
abstract class ImmediateNotificationIntegrationSupport : PostgreSqlIntegrationTest() {
	@Autowired lateinit var budgetService: BudgetStrategyService
	@Autowired lateinit var accountService: AccountService
	@Autowired lateinit var assetGoalService: AssetGoalService
	@Autowired lateinit var monthlyBudgetRepository: MonthlyBudgetRepository
	@Autowired lateinit var settingRepository: NotificationSettingRepository
	@Autowired lateinit var eventPublisher: ApplicationEventPublisher
	@Autowired lateinit var jdbcTemplate: JdbcTemplate

	protected lateinit var userId: UUID

	@BeforeEach
	fun isolateDatabase() {
		jdbcTemplate.execute(
			"""
				TRUNCATE TABLE
					monthly_budgets,
					budget_expenses,
					asset_goals,
					saving_records,
					accounts,
					notifications,
					notification_policy_markers,
					notification_settings,
					notification_preferences,
					balance_shortage_states
				RESTART IDENTITY CASCADE
			""".trimIndent(),
		)
		userId = persistUser().id
	}

	protected fun assertBudgetThresholdScenario() {
		val budget = createBudget(usedAmount = 70L)

		budgetService.updateUsedAmount(userId, budget.id, UpdateUsedAmountCommand(105L))

		assertThat(monthlyBudgetRepository.findByIdAndUserId(budget.id, userId)!!.usedAmount).isEqualTo(105L)
		assertThat(notificationTypes()).containsExactly(NotificationType.BUDGET_EXCEEDED.name)
		assertThat(markerKeys()).containsExactlyInAnyOrder("BUDGET:${budget.id}:80", "BUDGET:${budget.id}:100")
	}

	protected fun assertBudgetEventReentryIsNoOp() {
		val budget = createBudget(usedAmount = 70L)
		budgetService.updateUsedAmount(userId, budget.id, UpdateUsedAmountCommand(105L))

		eventPublisher.publishEvent(
			BudgetUsageChangedEvent(
				userId = userId,
				monthlyBudgetId = budget.id,
				previousBudgetAmount = 100L,
				previousUsedAmount = 70L,
				currentBudgetAmount = 100L,
				currentUsedAmount = 105L,
				occurredAt = Instant.parse("2026-09-03T00:00:00Z"),
			),
		)

		assertThat(notificationTypes()).containsExactly(NotificationType.BUDGET_EXCEEDED.name)
		assertThat(markerKeys()).containsExactlyInAnyOrder("BUDGET:${budget.id}:80", "BUDGET:${budget.id}:100")
	}

	protected fun assertDisabledBudgetTypeStillClaimsMarkers() {
		settingRepository.upsert(userId, NotificationType.BUDGET_EXCEEDED, false)
		val budget = createBudget(usedAmount = 70L)

		budgetService.updateUsedAmount(userId, budget.id, UpdateUsedAmountCommand(105L))

		assertThat(notificationTypes()).isEmpty()
		assertThat(markerKeys()).containsExactlyInAnyOrder("BUDGET:${budget.id}:80", "BUDGET:${budget.id}:100")
	}

	protected fun assertAccountChangeTriggersGoalMilestone() {
		val goal = assetGoalService.createAssetGoal(
			userId,
			CreateAssetGoalCommand("주택 자금", 100L, LocalDate.of(2027, 1, 1)),
		)

		val account = accountService.create(userId, CreateAccountCommand("입출금", AccountType.LIQUID, 105L))

		assertThat(jdbcTemplate.queryForObject("SELECT balance FROM accounts WHERE id = ?", Long::class.java, account.id))
			.isEqualTo(105L)
		assertThat(notificationTypes()).containsExactly(NotificationType.ASSET_GOAL_ACHIEVED.name)
		assertThat(markerKeys()).containsExactlyInAnyOrder(
			"ASSET_GOAL:${goal.id}:50",
			"ASSET_GOAL:${goal.id}:80",
			"ASSET_GOAL:${goal.id}:100",
		)
	}

	protected fun createBudget(usedAmount: Long) = budgetService.create(
		userId,
		CreateMonthlyBudgetCommand("2026-09", 100L, usedAmount),
	)

	protected fun notificationTypes(): List<String> = jdbcTemplate.queryForList(
		"SELECT type FROM notifications ORDER BY created_at, id",
		String::class.java,
	)

	protected fun markerKeys(): List<String> = jdbcTemplate.queryForList(
		"SELECT policy_key FROM notification_policy_markers ORDER BY policy_key",
		String::class.java,
	)
}

@Disabled("V8__enforce_user_ownership.sql(Stage 3.5) 적용 후 활성화. ON CONFLICT (user_id, ...) 대상 제약이 아직 없다.")
class ImmediateNotificationIntegrationTest : ImmediateNotificationIntegrationSupport() {
	@Test
	fun `동시 지출 추가는 예산 사용액과 marker와 알림을 일관되게 저장한다`() {
		val budget = createBudget(usedAmount = 70L)
		val ready = CountDownLatch(2)
		val start = CountDownLatch(1)
		val executor = Executors.newFixedThreadPool(2)

		try {
			val futures = listOf(20L, 40L).map { amount ->
				executor.submit {
					ready.countDown()
					check(start.await(10, TimeUnit.SECONDS))
					budgetService.addExpense(
						userId,
						budget.id,
						CreateBudgetExpenseCommand(amount, "식비", null, LocalDate.of(2026, 9, 2)),
					)
				}
			}

			check(ready.await(10, TimeUnit.SECONDS))
			start.countDown()
			futures.forEach { it.get(10, TimeUnit.SECONDS) }
		} finally {
			start.countDown()
			executor.shutdownNow()
			check(executor.awaitTermination(10, TimeUnit.SECONDS))
		}

		assertThat(monthlyBudgetRepository.findByIdAndUserId(budget.id, userId)!!.usedAmount).isEqualTo(130L)
		assertThat(jdbcTemplate.queryForObject(
			"SELECT COUNT(*) FROM budget_expenses WHERE monthly_budget_id = ?",
			Int::class.java,
			budget.id,
		)).isEqualTo(2)
		assertThat(markerKeys()).containsExactlyInAnyOrder("BUDGET:${budget.id}:80", "BUDGET:${budget.id}:100")
		assertThat(notificationTypes()).contains(NotificationType.BUDGET_EXCEEDED.name)
		assertThat(notificationTypes()).doesNotHaveDuplicates()
		assertThat(notificationTypes()).isIn(
			listOf(NotificationType.BUDGET_EXCEEDED.name),
			listOf(NotificationType.BUDGET_WARNING.name, NotificationType.BUDGET_EXCEEDED.name),
		)
	}

	@Test
	fun `예산이 70에서 105퍼센트가 되면 초과 알림 하나와 marker 둘을 저장한다`() = assertBudgetThresholdScenario()

	@Test
	fun `같은 예산 이벤트를 재진입해도 marker와 알림을 중복 저장하지 않는다`() = assertBudgetEventReentryIsNoOp()

	@Test
	fun `유형이 비활성화되어도 예산 marker는 저장하고 알림은 만들지 않는다`() = assertDisabledBudgetTypeStillClaimsMarkers()

	@Test
	fun `계좌 잔액 변경은 자산 목표 최고 마일스톤 알림으로 연결된다`() = assertAccountChangeTriggersGoalMilestone()

	@Test
	fun `동시 계좌 생성은 자산 목표 50퍼센트 marker와 알림을 놓치지 않는다`() {
		val goal = assetGoalService.createAssetGoal(
			userId,
			CreateAssetGoalCommand("주택 자금", 100L, LocalDate.of(2027, 1, 1)),
		)
		val ready = CountDownLatch(2)
		val start = CountDownLatch(1)
		val executor = Executors.newFixedThreadPool(2)

		try {
			val futures = listOf("입출금", "저축").map { name ->
				executor.submit {
					ready.countDown()
					check(start.await(10, TimeUnit.SECONDS))
					accountService.create(userId, CreateAccountCommand(name, AccountType.LIQUID, 30L))
				}
			}

			check(ready.await(10, TimeUnit.SECONDS))
			start.countDown()
			futures.forEach { it.get(10, TimeUnit.SECONDS) }
		} finally {
			start.countDown()
			executor.shutdownNow()
			check(executor.awaitTermination(10, TimeUnit.SECONDS))
		}

		assertThat(jdbcTemplate.queryForObject("SELECT COALESCE(SUM(balance), 0) FROM accounts", Long::class.java))
			.isEqualTo(60L)
		assertThat(notificationTypes()).containsExactly(NotificationType.ASSET_GOAL_PROGRESS.name)
		assertThat(markerKeys()).containsExactly("ASSET_GOAL:${goal.id}:50")
	}

	@Test
	fun `동시 계좌와 목표 변경은 최종 50퍼센트 자산 목표 marker와 알림을 남긴다`() {
		val account = accountService.create(userId, CreateAccountCommand("입출금", AccountType.LIQUID, 40L))
		val goal = assetGoalService.createAssetGoal(
			userId,
			CreateAssetGoalCommand("주택 자금", 100L, LocalDate.of(2027, 1, 1)),
		)
		val ready = CountDownLatch(2)
		val start = CountDownLatch(1)
		val executor = Executors.newFixedThreadPool(2)

		try {
			val accountUpdate = executor.submit {
				ready.countDown()
				check(start.await(10, TimeUnit.SECONDS))
				accountService.update(userId, account.id, UpdateAccountCommand("입출금", AccountType.LIQUID, 45L))
			}
			val goalUpdate = executor.submit {
				ready.countDown()
				check(start.await(10, TimeUnit.SECONDS))
				assetGoalService.updateAssetGoal(
					userId,
					goal.id,
					UpdateAssetGoalCommand("주택 자금", 90L, LocalDate.of(2027, 1, 1)),
				)
			}

			check(ready.await(10, TimeUnit.SECONDS))
			start.countDown()
			accountUpdate.get(10, TimeUnit.SECONDS)
			goalUpdate.get(10, TimeUnit.SECONDS)
		} finally {
			start.countDown()
			executor.shutdownNow()
			check(executor.awaitTermination(10, TimeUnit.SECONDS))
		}

		assertThat(jdbcTemplate.queryForObject("SELECT balance FROM accounts WHERE id = ?", Long::class.java, account.id))
			.isEqualTo(45L)
		assertThat(jdbcTemplate.queryForObject("SELECT target_amount FROM asset_goals WHERE id = ?", Long::class.java, goal.id))
			.isEqualTo(90L)
		assertThat(notificationTypes()).containsExactly(NotificationType.ASSET_GOAL_PROGRESS.name)
		assertThat(markerKeys()).containsExactly("ASSET_GOAL:${goal.id}:50")
	}
}

@Disabled("V8__enforce_user_ownership.sql(Stage 3.5) 적용 후 활성화. ON CONFLICT (user_id, ...) 대상 제약이 아직 없다.")
@Import(FailingNotificationGenerationConfig::class)
class ImmediateNotificationRollbackIntegrationTest : ImmediateNotificationIntegrationSupport() {
	@Test
	fun `알림 생성 실패는 예산 변경과 marker와 notification을 모두 롤백한다`() {
		val budget = createBudget(usedAmount = 70L)

		assertThatThrownBy {
			budgetService.updateUsedAmount(userId, budget.id, UpdateUsedAmountCommand(105L))
		}.isInstanceOf(IllegalStateException::class.java)

		assertThat(monthlyBudgetRepository.findByIdAndUserId(budget.id, userId)!!.usedAmount).isEqualTo(70L)
		assertThat(notificationTypes()).isEmpty()
		assertThat(markerKeys()).isEmpty()
	}
}

@TestConfiguration
class FailingNotificationGenerationConfig {
	@Bean
	@Primary
	fun failingNotificationGenerationService(): NotificationGenerationService = object : NotificationGenerationService {
		override fun createIfEnabled(userId: UUID, candidate: NewNotification): Boolean {
			throw IllegalStateException("forced notification failure")
		}
	}
}
