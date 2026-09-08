package com.knowave.cashboard.domains.notification.job

import com.knowave.cashboard.domains.financialschedule.calculator.FinancialCashFlowCalculator
import com.knowave.cashboard.domains.financialschedule.context.LiquidityBalanceProvider
import com.knowave.cashboard.domains.financialschedule.service.CalendarOccurrenceSource
import com.knowave.cashboard.domains.notification.policy.BalanceShortagePolicy
import com.knowave.cashboard.domains.notification.repository.BalanceShortageStateRepository
import com.knowave.cashboard.domains.notification.scheduler.NotificationScheduleContext
import com.knowave.cashboard.domains.notification.service.NotificationGenerationService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class BalanceShortageNotificationJob(
	private val occurrenceSource: CalendarOccurrenceSource,
	private val liquidityBalanceProvider: LiquidityBalanceProvider,
	private val policy: BalanceShortagePolicy,
	private val stateRepository: BalanceShortageStateRepository,
	private val generationService: NotificationGenerationService,
) : ScheduledNotificationJob {
	private val calculator = FinancialCashFlowCalculator()

	override val name: String = "balance-shortage"

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	override fun run(context: NotificationScheduleContext) {
		val occurrences = occurrenceSource.findOccurrences(context.date, context.date.plusDays(30))
		val openingBalance = liquidityBalanceProvider.getCurrentLiquidBalance()
		val projection = calculator.project(context.date, openingBalance, occurrences)
		val shortage = policy.findFirstShortage(openingBalance, context.date, projection)
		val transition = stateRepository.transition(SINGLE_USER_SCOPE_KEY, shortage?.date)
		if (transition.newEpisode && shortage != null) {
			generationService.createIfEnabled(policy.toNotification(shortage, transition.episode, context.scheduledAt))
		}
	}

	private companion object {
		const val SINGLE_USER_SCOPE_KEY = "SINGLE_USER"
	}
}
