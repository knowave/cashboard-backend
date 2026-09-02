package com.knowave.cashboard.domains.financialschedule.service

import com.knowave.cashboard.domains.financialschedule.service.dto.CreateFinancialScheduleCommand
import com.knowave.cashboard.domains.financialschedule.service.dto.FinancialScheduleResult
import com.knowave.cashboard.domains.financialschedule.service.dto.PatchFinancialScheduleCommand
import java.util.UUID

interface FinancialScheduleService {
	fun create(command: CreateFinancialScheduleCommand): FinancialScheduleResult
	fun get(id: UUID): FinancialScheduleResult
	fun getAll(): List<FinancialScheduleResult>
	fun patch(id: UUID, command: PatchFinancialScheduleCommand): FinancialScheduleResult
	fun delete(id: UUID)
}
