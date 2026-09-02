package com.knowave.cashboard.domains.financialschedule.service

import com.knowave.cashboard.domains.financialschedule.calculator.ScheduleOccurrence
import java.time.LocalDate

interface CalendarOccurrenceSource {
	fun findOccurrences(from: LocalDate, toInclusive: LocalDate): List<ScheduleOccurrence>
}
