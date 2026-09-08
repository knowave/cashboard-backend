package com.knowave.cashboard.domains.notification

import com.knowave.cashboard.domains.financialschedule.service.FinancialScheduleService
import com.knowave.cashboard.domains.financialschedule.service.dto.CreateFinancialScheduleCommand
import com.knowave.cashboard.domains.financialschedule.service.dto.RecurrenceCommand
import com.knowave.cashboard.domains.notification.job.ScheduledNotificationJob
import com.knowave.cashboard.domains.notification.scheduler.NotificationScheduleContext
import com.knowave.cashboard.domains.notification.scheduler.NotificationScheduler
import com.knowave.cashboard.support.PostgreSqlIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.aop.support.AopUtils
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset

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
		jdbcTemplate.execute(
			"""
				CREATE TABLE IF NOT EXISTS accounts (
					id UUID PRIMARY KEY,
					name VARCHAR(255) NOT NULL,
					type VARCHAR(50) NOT NULL,
					balance BIGINT NOT NULL,
					created_at TIMESTAMP NOT NULL,
					updated_at TIMESTAMP NOT NULL
				)
			""".trimIndent(),
		)
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
					scheduled_job_failure_probes
				RESTART IDENTITY CASCADE
			""".trimIndent(),
		)
	}

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
		financialScheduleService.create(
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
