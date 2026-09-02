package com.knowave.cashboard.domains.financialschedule.service

import com.knowave.cashboard.domains.financialschedule.service.dto.FinancialCalendarResult

interface FinancialCalendarService {
	fun getCalendar(year: Int, month: Int): FinancialCalendarResult
}
