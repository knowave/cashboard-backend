package com.knowave.cashboard.domains.notification.policy

import com.knowave.cashboard.domains.financialschedule.calculator.ScheduleOccurrence
import com.knowave.cashboard.domains.financialschedule.entity.CashFlowDirection
import com.knowave.cashboard.domains.notification.entity.NotificationType
import com.knowave.cashboard.domains.notification.repository.NewNotification
import org.springframework.stereotype.Component
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.UUID

@Component
class PaymentDuePolicy {
	fun evaluate(baseDate: LocalDate, occurrence: ScheduleOccurrence, scheduledAt: Instant, userId: UUID): NewNotification? {
		if (occurrence.direction != CashFlowDirection.EXPENSE) return null

		val offset = ChronoUnit.DAYS.between(baseDate, occurrence.date).toInt()
		if (offset !in OFFSETS) return null

		return NewNotification(
			userId = userId,
			type = NotificationType.PAYMENT_DUE,
			title = "결제 예정 알림",
			message = paymentMessage(occurrence, offset),
			scheduledAt = scheduledAt,
			deduplicationKey = "PAYMENT_DUE:${occurrence.scheduleId}:${occurrence.date}:$offset",
		)
	}

	private fun paymentMessage(occurrence: ScheduleOccurrence, offset: Int): String =
		"${offsetLabel(offset)} ${occurrence.title} ${NUMBER_FORMAT.format(occurrence.amount)}원이 결제 예정이에요."

	private fun offsetLabel(offset: Int): String = when (offset) {
		0 -> "오늘"
		1 -> "내일"
		else -> "${offset}일 후"
	}

	private companion object {
		val OFFSETS = setOf(0, 1, 3)
		val NUMBER_FORMAT: NumberFormat = NumberFormat.getNumberInstance(Locale.KOREA)
	}
}
