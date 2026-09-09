package com.knowave.cashboard.domains.notification.job

import com.knowave.cashboard.domains.notification.repository.NewNotification
import com.knowave.cashboard.domains.notification.report.WeeklyReportProvider
import com.knowave.cashboard.domains.notification.scheduler.NotificationScheduleContext
import com.knowave.cashboard.domains.notification.service.NotificationGenerationService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

class WeeklyReportNotificationJobTest {
	@Test
	fun `월요일에만 지난주 리포트를 멱등 후보로 생성한다`() {
		val generationService = RecordingGenerationService()
		val provider = WeeklyReportProvider(EmptyWeeklyExpenseRepository(), EmptyWeeklyBudgetRepository())
		val job = WeeklyReportNotificationJob(provider, generationService)
		val context = NotificationScheduleContext(LocalDate.of(2026, 9, 7), Instant.parse("2026-09-07T00:00:00Z"), ZoneId.of("Asia/Seoul"))

		job.run(context)
		job.run(context)

		assertThat(generationService.created).hasSize(1)
		val notification = generationService.created.single()
		assertThat(notification.deduplicationKey).isEqualTo("WEEKLY_REPORT:2026-08-31")
		assertThat(notification.scheduledAt).isEqualTo(context.scheduledAt)
	}

	@Test
	fun `월요일이 아니면 리포트를 생성하지 않는다`() {
		val generationService = RecordingGenerationService()
		val job = WeeklyReportNotificationJob(WeeklyReportProvider(EmptyWeeklyExpenseRepository(), EmptyWeeklyBudgetRepository()), generationService)

		job.run(NotificationScheduleContext(LocalDate.of(2026, 9, 8), Instant.parse("2026-09-08T00:00:00Z"), ZoneId.of("Asia/Seoul")))

		assertThat(generationService.created).isEmpty()
	}

	@Test
	fun `Job은 다른 예정 알림 정책과 분리된 새 트랜잭션으로 실행된다`() {
		val transaction = requireNotNull(WeeklyReportNotificationJob::class.java
			.getDeclaredMethod("run", NotificationScheduleContext::class.java)
			.getAnnotation(Transactional::class.java))

		assertThat(transaction.propagation).isEqualTo(Propagation.REQUIRES_NEW)
	}
}

private class EmptyWeeklyExpenseRepository : com.knowave.cashboard.domains.expenseanalysis.repository.ExpenseAnalysisRepository {
	override fun findCategoryExpenses(userId: UUID, start: LocalDate, end: LocalDate) = emptyList<com.knowave.cashboard.domains.expenseanalysis.repository.dto.CategoryExpenseProjection>()
	override fun findMonthlyExpenses(userId: UUID, start: LocalDate, end: LocalDate) = emptyList<com.knowave.cashboard.domains.expenseanalysis.repository.dto.MonthlyExpenseProjection>()
}

private class EmptyWeeklyBudgetRepository : com.knowave.cashboard.domains.budget.repository.MonthlyBudgetRepository {
	override fun save(monthlyBudget: com.knowave.cashboard.domains.budget.entity.MonthlyBudget) = monthlyBudget
	override fun findByIdAndUserId(id: UUID, userId: UUID) = null
	override fun findByIdForUpdate(id: UUID, userId: UUID) = null
	override fun findByTargetMonthAndUserId(targetMonth: String, userId: UUID) = null
	override fun existsByIdAndUserId(id: UUID, userId: UUID) = false
	override fun existsByTargetMonthAndUserId(targetMonth: String, userId: UUID) = false
}

private class RecordingGenerationService : NotificationGenerationService {
	val created = mutableListOf<NewNotification>()
	override fun createIfEnabled(userId: UUID, candidate: NewNotification): Boolean {
		if (created.any { it.deduplicationKey == candidate.deduplicationKey }) return false
		created += candidate
		return true
	}
}
