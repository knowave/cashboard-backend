package com.knowave.cashboard.domains.financialschedule.service

import com.knowave.cashboard.domains.financialschedule.calculator.ScheduleOccurrence
import java.time.LocalDate
import java.util.UUID

interface CalendarOccurrenceSource {
	fun findOccurrences(userId: UUID, from: LocalDate, toInclusive: LocalDate): List<ScheduleOccurrence>
}
