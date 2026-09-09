package com.knowave.cashboard.domains.notification.job

import com.knowave.cashboard.domains.notification.report.MonthlyReportProvider
import com.knowave.cashboard.domains.notification.scheduler.NotificationScheduleContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

class MonthlyReportNotificationJobTest {
	@Test
	fun `매월 1일에만 지난달 리포트를 멱등 후보로 생성한다`() {
		val generationService = MonthlyRecordingGenerationService()
		val job = MonthlyReportNotificationJob(MonthlyReportProvider(MonthlyEmptyAnalysisService(), MonthlyEmptyGoalRepository(), MonthlyEmptyAccountRepository(), com.knowave.cashboard.domains.assetgoal.calculator.AssetGoalCalculator()), generationService)
		val context = NotificationScheduleContext(LocalDate.of(2026, 9, 1), Instant.parse("2026-09-01T00:00:00Z"), ZoneId.of("Asia/Seoul"))

		job.run(context)
		job.run(context)

		assertThat(generationService.created).hasSize(1)
		assertThat(generationService.created.single().deduplicationKey).isEqualTo("MONTHLY_REPORT:2026-08")
	}

	@Test
	fun `매월 1일이 아니면 리포트를 생성하지 않는다`() {
		val generationService = MonthlyRecordingGenerationService()
		val job = MonthlyReportNotificationJob(MonthlyReportProvider(MonthlyEmptyAnalysisService(), MonthlyEmptyGoalRepository(), MonthlyEmptyAccountRepository(), com.knowave.cashboard.domains.assetgoal.calculator.AssetGoalCalculator()), generationService)

		job.run(NotificationScheduleContext(LocalDate.of(2026, 9, 2), Instant.parse("2026-09-02T00:00:00Z"), ZoneId.of("Asia/Seoul")))

		assertThat(generationService.created).isEmpty()
	}

	@Test
	fun `Job은 다른 예정 알림 정책과 분리된 새 트랜잭션으로 실행된다`() {
		val transaction = requireNotNull(MonthlyReportNotificationJob::class.java
			.getDeclaredMethod("run", NotificationScheduleContext::class.java)
			.getAnnotation(Transactional::class.java))

		assertThat(transaction.propagation).isEqualTo(Propagation.REQUIRES_NEW)
	}
}

private class MonthlyEmptyAnalysisService : com.knowave.cashboard.domains.expenseanalysis.service.ExpenseAnalysisService {
	override fun getAnalysis(userId: UUID, year: Int, month: Int) = com.knowave.cashboard.domains.expenseanalysis.service.dto.ExpenseAnalysisResult(
		com.knowave.cashboard.domains.expenseanalysis.service.dto.PeriodResult(year, month), 0,
		com.knowave.cashboard.domains.expenseanalysis.service.dto.ExpenseComparisonResult(0, 0, null),
		com.knowave.cashboard.domains.expenseanalysis.service.dto.RecentAverageResult(0, 0), emptyList(), emptyList(),
	)
}

private class MonthlyEmptyGoalRepository : com.knowave.cashboard.domains.assetgoal.repository.AssetGoalRepository {
	override fun save(assetGoal: com.knowave.cashboard.domains.assetgoal.entity.AssetGoal) = assetGoal
	override fun findByIdAndUserId(id: UUID, userId: UUID) = null
	override fun findAllByUserId(userId: UUID) = emptyList<com.knowave.cashboard.domains.assetgoal.entity.AssetGoal>()
	override fun delete(assetGoal: com.knowave.cashboard.domains.assetgoal.entity.AssetGoal) = Unit
}

private class MonthlyEmptyAccountRepository : com.knowave.cashboard.domains.account.repository.AccountRepository {
	override fun save(account: com.knowave.cashboard.domains.account.entity.Account) = account
	override fun findByIdAndUserId(id: UUID, userId: UUID) = null
	override fun findAllByUserId(userId: UUID) = emptyList<com.knowave.cashboard.domains.account.entity.Account>()
	override fun delete(account: com.knowave.cashboard.domains.account.entity.Account) = Unit
}

private class MonthlyRecordingGenerationService : com.knowave.cashboard.domains.notification.service.NotificationGenerationService {
	val created = mutableListOf<com.knowave.cashboard.domains.notification.repository.NewNotification>()
	override fun createIfEnabled(userId: UUID, candidate: com.knowave.cashboard.domains.notification.repository.NewNotification): Boolean {
		if (created.any { it.deduplicationKey == candidate.deduplicationKey }) return false
		created += candidate
		return true
	}
}
