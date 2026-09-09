package com.knowave.cashboard.domains.notification.job

import com.knowave.cashboard.domains.notification.entity.NotificationType
import com.knowave.cashboard.domains.notification.report.WeeklyReportProvider
import com.knowave.cashboard.domains.notification.repository.NewNotification
import com.knowave.cashboard.domains.notification.scheduler.NotificationScheduleContext
import com.knowave.cashboard.domains.notification.service.NotificationGenerationService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek

@Component
class WeeklyReportNotificationJob(
	private val provider: WeeklyReportProvider,
	private val generationService: NotificationGenerationService,
) : ScheduledNotificationJob {
	override val name: String = "weekly-report"

	// ponytail: NotificationScheduleContext has no userId yet (Stage 4, out of this task's
	// scope). PLACEHOLDER_USER_ID keeps this compiling as a single implicit user.
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	override fun run(context: NotificationScheduleContext) {
		if (context.date.dayOfWeek != DayOfWeek.MONDAY) return

		val userId = PLACEHOLDER_USER_ID
		val summary = provider.generate(userId, context.date)
		generationService.createIfEnabled(
			userId,
			NewNotification(
				userId = userId,
				type = NotificationType.WEEKLY_REPORT,
				title = "지난주 소비 리포트가 도착했어요",
				message = summary.toMessage(),
				scheduledAt = context.scheduledAt,
				deduplicationKey = "WEEKLY_REPORT:${summary.weekStart}",
			),
		)
	}
}
