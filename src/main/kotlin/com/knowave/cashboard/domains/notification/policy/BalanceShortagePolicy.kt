package com.knowave.cashboard.domains.notification.policy

import com.knowave.cashboard.domains.financialschedule.calculator.CashFlowProjection
import com.knowave.cashboard.domains.notification.entity.NotificationType
import com.knowave.cashboard.domains.notification.repository.NewNotification
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDate

data class BalanceShortage(
	val date: LocalDate,
	val expectedBalance: Long,
)

@Component
class BalanceShortagePolicy {
	fun findFirstShortage(
		openingBalance: Long,
		projectionStartDate: LocalDate,
		projection: CashFlowProjection,
	): BalanceShortage? {
		if (openingBalance < 0) return BalanceShortage(projectionStartDate, openingBalance)
		return findFirstShortage(projection)
	}

	fun findFirstShortage(projection: CashFlowProjection): BalanceShortage? = projection.dailyBalances
		.firstOrNull { it.expectedClosingBalance < 0 }
		?.let { BalanceShortage(it.date, it.expectedClosingBalance) }

	fun toNotification(shortage: BalanceShortage, episode: Long, scheduledAt: Instant): NewNotification = NewNotification(
		type = NotificationType.BALANCE_SHORTAGE,
		title = "예상 잔액이 부족해요",
		message = "${shortage.date}에 예상 잔액이 ${shortage.expectedBalance}원으로 부족할 수 있어요.",
		scheduledAt = scheduledAt,
		deduplicationKey = "BALANCE_SHORTAGE:$episode:${shortage.date}",
	)
}
