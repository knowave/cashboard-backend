package com.knowave.cashboard.domains.notification.job

import com.knowave.cashboard.domains.notification.entity.NotificationType
import com.knowave.cashboard.domains.notification.report.MonthlyReportProvider
import com.knowave.cashboard.domains.notification.repository.NewNotification
import com.knowave.cashboard.domains.notification.scheduler.NotificationScheduleContext
import com.knowave.cashboard.domains.notification.service.NotificationGenerationService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class MonthlyReportNotificationJob(
	private val provider: MonthlyReportProvider,
	private val generationService: NotificationGenerationService,
) : ScheduledNotificationJob {
	override val name: String = "monthly-report"

	// ponytail: NotificationScheduleContext has no userId yet (Stage 4, out of this task's
	// scope). PLACEHOLDER_USER_ID keeps this compiling as a single implicit user.
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	override fun run(context: NotificationScheduleContext) {
		if (context.date.dayOfMonth != 1) return

		val userId = PLACEHOLDER_USER_ID
		val summary = provider.generate(userId, context.date)
		generationService.createIfEnabled(
			userId,
			NewNotification(
				userId = userId,
				type = NotificationType.MONTHLY_REPORT,
				title = "지난달 소비 리포트가 도착했어요",
				message = summary.toMessage(),
				scheduledAt = context.scheduledAt,
				deduplicationKey = "MONTHLY_REPORT:${summary.yearMonth}",
			),
		)
	}
}
