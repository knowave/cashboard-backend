package com.knowave.cashboard.domains.financialschedule.entity

import com.knowave.cashboard.common.exception.InvalidFinancialSchedulePeriodException
import com.knowave.cashboard.common.exception.InvalidRecurrenceRuleException
import java.time.DateTimeException
import java.time.LocalDate
import java.time.MonthDay

sealed interface RecurrenceRule {
	data class Once(val scheduledDate: LocalDate) : RecurrenceRule

	data class Monthly(
		val dayOfMonth: Int,
		val startDate: LocalDate,
		val endDate: LocalDate?,
	) : RecurrenceRule {
		init {
			if (dayOfMonth !in 1..31) {
				throw InvalidRecurrenceRuleException("dayOfMonth must be between 1 and 31.")
			}
			validatePeriod(startDate, endDate)
		}
	}

	data class Yearly(
		val monthOfYear: Int,
		val dayOfMonth: Int,
		val startDate: LocalDate,
		val endDate: LocalDate?,
	) : RecurrenceRule {
		init {
			try {
				MonthDay.of(monthOfYear, dayOfMonth)
			} catch (exception: DateTimeException) {
				throw InvalidRecurrenceRuleException("monthOfYear and dayOfMonth must form a valid month-day.")
			}
			validatePeriod(startDate, endDate)
		}
	}
}

private fun validatePeriod(startDate: LocalDate, endDate: LocalDate?) {
	if (endDate != null && endDate.isBefore(startDate)) {
		throw InvalidFinancialSchedulePeriodException("endDate must not be before startDate.")
	}
}
