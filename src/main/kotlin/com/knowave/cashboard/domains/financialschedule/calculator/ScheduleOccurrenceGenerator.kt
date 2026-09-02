package com.knowave.cashboard.domains.financialschedule.calculator

import com.knowave.cashboard.common.exception.InvalidFinancialSchedulePeriodException
import com.knowave.cashboard.domains.financialschedule.entity.RecurrenceRule
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.YearMonth

@Component
class ScheduleOccurrenceGenerator {
	fun generate(
		definition: ScheduleDefinition,
		from: LocalDate,
		toInclusive: LocalDate,
	): List<ScheduleOccurrence> {
		if (toInclusive.isBefore(from)) {
			throw InvalidFinancialSchedulePeriodException("toInclusive must not be before from.")
		}

		val dates = when (val rule = definition.recurrence) {
			is RecurrenceRule.Once -> listOfNotNull(rule.scheduledDate.takeIf { it in from..toInclusive })
			is RecurrenceRule.Monthly -> monthlyDates(rule, from, toInclusive)
			is RecurrenceRule.Yearly -> yearlyDates(rule, from, toInclusive)
		}

		return dates.map { definition.toOccurrence(it) }
			.sortedWith(compareBy(ScheduleOccurrence::date, ScheduleOccurrence::title, ScheduleOccurrence::scheduleId))
	}

	private fun monthlyDates(
		rule: RecurrenceRule.Monthly,
		from: LocalDate,
		toInclusive: LocalDate,
	): List<LocalDate> {
		val firstMonth = YearMonth.from(maxOf(from, rule.startDate))
		val lastMonth = YearMonth.from(minOf(toInclusive, rule.endDate ?: toInclusive))
		if (lastMonth.isBefore(firstMonth)) return emptyList()

		return generateSequence(firstMonth) { it.plusMonths(1) }
			.takeWhile { !it.isAfter(lastMonth) }
			.map { month -> month.atDay(minOf(rule.dayOfMonth, month.lengthOfMonth())) }
			.filter { it >= rule.startDate && (rule.endDate == null || it <= rule.endDate) && it in from..toInclusive }
			.toList()
	}

	private fun yearlyDates(
		rule: RecurrenceRule.Yearly,
		from: LocalDate,
		toInclusive: LocalDate,
	): List<LocalDate> {
		val firstYear = maxOf(from, rule.startDate).year
		val lastYear = minOf(toInclusive, rule.endDate ?: toInclusive).year
		if (lastYear < firstYear) return emptyList()

		return (firstYear..lastYear).map { year ->
			val month = YearMonth.of(year, rule.monthOfYear)
			month.atDay(minOf(rule.dayOfMonth, month.lengthOfMonth()))
		}.filter { it >= rule.startDate && (rule.endDate == null || it <= rule.endDate) && it in from..toInclusive }
	}

	private fun ScheduleDefinition.toOccurrence(date: LocalDate) = ScheduleOccurrence(
		scheduleId = scheduleId,
		date = date,
		type = type,
		title = title,
		amount = amount,
		direction = direction,
	)
}
