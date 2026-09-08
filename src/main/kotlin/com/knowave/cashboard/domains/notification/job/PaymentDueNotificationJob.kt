package com.knowave.cashboard.domains.notification.job

import com.knowave.cashboard.domains.financialschedule.service.CalendarOccurrenceSource
import com.knowave.cashboard.domains.notification.policy.PaymentDuePolicy
import com.knowave.cashboard.domains.notification.scheduler.NotificationScheduleContext
import com.knowave.cashboard.domains.notification.service.NotificationGenerationService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class PaymentDueNotificationJob(
	private val occurrenceSource: CalendarOccurrenceSource,
	private val policy: PaymentDuePolicy,
	private val generationService: NotificationGenerationService,
) : ScheduledNotificationJob {
	override val name: String = "payment-due"

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	override fun run(context: NotificationScheduleContext) {
		occurrenceSource.findOccurrences(context.date, context.date.plusDays(3))
			.mapNotNull { policy.evaluate(context.date, it, context.scheduledAt) }
			.forEach(generationService::createIfEnabled)
	}
}
