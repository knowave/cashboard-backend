package com.knowave.cashboard.domains.financialschedule.service

import com.knowave.cashboard.common.exception.CashboardException
import com.knowave.cashboard.common.exception.FinancialScheduleDataIntegrityException
import com.knowave.cashboard.common.exception.InvalidFinancialSchedulePeriodException
import com.knowave.cashboard.domains.financialschedule.calculator.ScheduleDefinition
import com.knowave.cashboard.domains.financialschedule.calculator.ScheduleOccurrence
import com.knowave.cashboard.domains.financialschedule.calculator.ScheduleOccurrenceGenerator
import com.knowave.cashboard.domains.financialschedule.entity.CashFlowDirection
import com.knowave.cashboard.domains.financialschedule.entity.FinancialSchedule
import com.knowave.cashboard.domains.financialschedule.entity.ScheduleType
import com.knowave.cashboard.domains.financialschedule.repository.FinancialScheduleRepository
import com.knowave.cashboard.domains.financialschedule.service.dto.RecurrenceCommand
import com.knowave.cashboard.domains.financialschedule.service.dto.toRecurrenceRule
import org.springframework.stereotype.Component
import org.slf4j.LoggerFactory
import java.time.LocalDate

@Component
class FinancialScheduleOccurrenceSource(
	private val financialScheduleRepository: FinancialScheduleRepository,
	private val scheduleOccurrenceGenerator: ScheduleOccurrenceGenerator,
) : CalendarOccurrenceSource {
	override fun findOccurrences(from: LocalDate, toInclusive: LocalDate): List<ScheduleOccurrence> {
		if (toInclusive.isBefore(from)) {
			throw InvalidFinancialSchedulePeriodException("toInclusive must not be before from.")
		}

		return financialScheduleRepository.findCandidates(from, toInclusive)
			.flatMap { schedule -> scheduleOccurrenceGenerator.generate(schedule.toDefinition(), from, toInclusive) }
			.sortedWith(compareBy(ScheduleOccurrence::date, ScheduleOccurrence::title, ScheduleOccurrence::scheduleId))
	}

	private fun FinancialSchedule.toDefinition(): ScheduleDefinition {
		val scheduleId = requireNotNull(id)
		val recurrenceCommand = RecurrenceCommand(
			type = recurrenceType,
			scheduledDate = scheduledDate,
			monthOfYear = monthOfYear,
			dayOfMonth = dayOfMonth,
			startDate = startDate,
			endDate = endDate,
		)
		return ScheduleDefinition(
			scheduleId = scheduleId,
			type = persistedValue("scheduleType", scheduleType) { ScheduleType.from(scheduleType) },
			title = title,
			amount = amount,
			direction = persistedValue("direction", direction) { CashFlowDirection.from(direction) },
			recurrence = persistedValue("recurrence", recurrenceCommand) { recurrenceCommand.toRecurrenceRule() },
		)
	}

	private fun <T> FinancialSchedule.persistedValue(field: String, value: Any?, convert: () -> T): T = try {
		convert()
	} catch (exception: CashboardException) {
		val scheduleId = requireNotNull(id)
		logger.error(
			"FinancialSchedule persisted value is invalid. scheduleId={}, field={}, value={}",
			scheduleId,
			field,
			value,
			exception,
		)
		throw FinancialScheduleDataIntegrityException(scheduleId, field, exception)
	}

	private companion object {
		val logger = LoggerFactory.getLogger(FinancialScheduleOccurrenceSource::class.java)
	}
}
