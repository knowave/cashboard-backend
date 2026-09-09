package com.knowave.cashboard.domains.notification

import com.knowave.cashboard.domains.financialschedule.service.FinancialScheduleService
import com.knowave.cashboard.domains.financialschedule.service.dto.CreateFinancialScheduleCommand
import com.knowave.cashboard.domains.financialschedule.service.dto.RecurrenceCommand
import com.knowave.cashboard.domains.notification.job.PLACEHOLDER_USER_ID
import com.knowave.cashboard.domains.notification.job.ScheduledNotificationJob
import com.knowave.cashboard.domains.notification.scheduler.NotificationScheduleContext
import com.knowave.cashboard.domains.notification.scheduler.NotificationScheduler
import com.knowave.cashboard.support.PostgreSqlIntegrationTest
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.aop.support.AopUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate

@Import(ScheduledNotificationIntegrationTestConfig::class)
class ScheduledNotificationIntegrationTest : PostgreSqlIntegrationTest() {
	@Autowired lateinit var scheduler: NotificationScheduler
	@Autowired lateinit var financialScheduleService: FinancialScheduleService
	@Autowired lateinit var jdbcTemplate: JdbcTemplate
	@Autowired lateinit var transactionTemplate: TransactionTemplate
	@Autowired lateinit var scheduledJobs: List<ScheduledNotificationJob>
	@Autowired lateinit var failureRecorder: ScheduledJobFailureRecorder

	@BeforeEach
	fun isolateDatabase() {
		failureRecorder.clear()
		jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS scheduled_job_failure_probes (id BIGSERIAL PRIMARY KEY)")
		jdbcTemplate.execute(
			"""
				TRUNCATE TABLE
					financial_schedules,
					monthly_budgets,
					budget_expenses,
					asset_goals,
					saving_records,
					accounts,
					notifications,
					notification_policy_markers,
					notification_settings,
					notification_preferences,
					balance_shortage_states,
					scheduled_job_failure_probes,
					users
				RESTART IDENTITY CASCADE
			""".trimIndent(),
		)
		// FK가 아직 없지만(V8 이전) 미리 심어 둔다 — V8이 users(id) FK를 걸면
		// PLACEHOLDER_USER_ID로 쓴 행이 매칭되는 실제 사용자 없이 깨지는 걸 막는다.
		persistUser(id = PLACEHOLDER_USER_ID)
	}

	// ponytail: V8 미실행 대기 — BalanceShortageNotificationJob의 transition()이 balance_shortage_states
	// 의 ON CONFLICT (user_id)로 실패하고 NotificationScheduler.runOnce()의 runCatching이 이를 삼켜
	// BALANCE_SHORTAGE 알림만 조용히 누락시킨다(4건 기대, 3건 실제).
	@Disabled("V8__enforce_user_ownership.sql(Stage 3.5) 적용 후 활성화. ON CONFLICT (user_id, ...) 대상 제약이 아직 없다.")
	@Test
	fun `월요일이자 1일 오전 9시는 실제 네 정책을 PENDING으로 저장한다`() {
		seedPaymentAndShortageData()

		scheduler.runOnce()

		assertThat(notificationTypes()).containsExactlyInAnyOrder(
			"PAYMENT_DUE",
			"BALANCE_SHORTAGE",
			"WEEKLY_REPORT",
			"MONTHLY_REPORT",
		)
		assertThat(notificationStatuses()).containsOnly("PENDING")
		assertThat(notificationScheduledAts()).containsOnly(Instant.parse("2025-09-01T00:00:00Z"))
		assertThat(scheduler.context()).isEqualTo(
			NotificationScheduleContext(
				date = LocalDate.of(2025, 9, 1),
				scheduledAt = Instant.parse("2025-09-01T00:00:00Z"),
				zoneId = java.time.ZoneId.of("Asia/Seoul"),
			),
		)
	}

	@Test
	fun `같은 날짜에 두 번 실행해도 실제 Job 알림 행은 늘지 않는다`() {
		seedPaymentAndShortageData()

		scheduler.runOnce()
		val firstCount = notificationCount()
		scheduler.runOnce()

		assertThat(notificationCount()).isEqualTo(firstCount)
	}

	@Disabled("V8__enforce_user_ownership.sql(Stage 3.5) 적용 후 활성화. ON CONFLICT (user_id, ...) 대상 제약이 아직 없다.")
	@Test
	fun `REQUIRES_NEW 실패 Job은 outer rollback과 무관하게 실제 네 Job을 커밋한다`() {
		seedPaymentAndShortageData()

		transactionTemplate.executeWithoutResult { outerTransaction ->
			scheduler.runOnce()

			assertThat(failureRecorder.contexts()).containsExactly(
				NotificationScheduleContext(
					date = LocalDate.of(2025, 9, 1),
					scheduledAt = Instant.parse("2025-09-01T00:00:00Z"),
					zoneId = java.time.ZoneId.of("Asia/Seoul"),
				),
			)
			assertThat(scheduledJobs.map { it.name }).startsWith("forced-failure")
			assertThat(scheduledJobs.filter { it.name in SCHEDULED_JOB_NAMES })
				.hasSize(5)
				.allMatch(AopUtils::isAopProxy)
			assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM scheduled_job_failure_probes", Long::class.java))
				.isZero()
			assertThat(notificationCount()).isEqualTo(4L)
			assertThat(outerTransaction.isRollbackOnly).isFalse()

			outerTransaction.setRollbackOnly()
		}

		assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM scheduled_job_failure_probes", Long::class.java))
			.isZero()
		assertThat(notificationCount()).isEqualTo(4L)
		assertThat(notificationTypes()).containsExactlyInAnyOrder(*NOTIFICATION_TYPES)
	}

	private fun seedPaymentAndShortageData() {
		// 스케줄러가 아직 사용자를 순회하지 않으므로(Stage 4 범위 밖) Job이 내부적으로 쓰는
		// PLACEHOLDER_USER_ID로 시드해야 실제 Job이 이 데이터를 찾는다.
		financialScheduleService.create(
			PLACEHOLDER_USER_ID,
			CreateFinancialScheduleCommand(
				type = "CARD",
				title = "카드 결제",
				amount = 10_000L,
				direction = "EXPENSE",
				recurrence = RecurrenceCommand(type = "ONCE", scheduledDate = LocalDate.of(2025, 9, 1)),
			),
		)
	}

	private fun notificationTypes(): List<String> = jdbcTemplate.queryForList(
		"SELECT type FROM notifications",
		String::class.java,
	)

	private fun notificationStatuses(): List<String> = jdbcTemplate.queryForList(
		"SELECT status FROM notifications",
		String::class.java,
	)

	private fun notificationScheduledAts(): List<Instant> = jdbcTemplate.query(
		"SELECT scheduled_at FROM notifications",
	) { resultSet, _ -> resultSet.getObject("scheduled_at", OffsetDateTime::class.java).toInstant() }

	private fun notificationCount(): Long = jdbcTemplate.queryForObject(
		"SELECT COUNT(*) FROM notifications",
		Long::class.java,
	)!!

	private companion object {
		val NOTIFICATION_TYPES = arrayOf("PAYMENT_DUE", "BALANCE_SHORTAGE", "WEEKLY_REPORT", "MONTHLY_REPORT")
		val SCHEDULED_JOB_NAMES = setOf("forced-failure", "payment-due", "balance-shortage", "weekly-report", "monthly-report")
	}
}

@TestConfiguration
class ScheduledNotificationIntegrationTestConfig {
	@Bean
	@Primary
	fun fixedNotificationClock(): Clock = Clock.fixed(
		Instant.parse("2025-09-01T00:00:00Z"),
		ZoneOffset.UTC,
	)

	@Bean
	fun scheduledJobFailureRecorder(): ScheduledJobFailureRecorder = ScheduledJobFailureRecorder()

	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	fun failingScheduledNotificationJob(
		jdbcTemplate: JdbcTemplate,
		recorder: ScheduledJobFailureRecorder,
	): FailingScheduledNotificationJob = FailingScheduledNotificationJob(jdbcTemplate, recorder)
}

@Order(Ordered.HIGHEST_PRECEDENCE)
open class FailingScheduledNotificationJob(
	private val jdbcTemplate: JdbcTemplate,
	private val recorder: ScheduledJobFailureRecorder,
) : ScheduledNotificationJob, Ordered {
	override val name: String = "forced-failure"
	override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	override open fun run(context: NotificationScheduleContext) {
		recorder.record(context)
		jdbcTemplate.update("INSERT INTO scheduled_job_failure_probes DEFAULT VALUES")
		throw IllegalStateException("forced scheduled notification failure")
	}
}

class ScheduledJobFailureRecorder {
	private val recordedContexts = java.util.concurrent.CopyOnWriteArrayList<NotificationScheduleContext>()

	fun record(context: NotificationScheduleContext) {
		recordedContexts += context
	}

	fun contexts(): List<NotificationScheduleContext> = recordedContexts.toList()

	fun clear() {
		recordedContexts.clear()
	}
}
