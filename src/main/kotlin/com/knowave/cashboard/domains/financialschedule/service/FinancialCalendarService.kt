package com.knowave.cashboard.domains.financialschedule.service

import com.knowave.cashboard.domains.financialschedule.service.dto.FinancialCalendarResult
import java.util.UUID

interface FinancialCalendarService {
	fun getCalendar(userId: UUID, year: Int, month: Int): FinancialCalendarResult
}
