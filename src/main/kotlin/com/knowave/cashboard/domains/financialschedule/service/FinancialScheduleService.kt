package com.knowave.cashboard.domains.financialschedule.service

import com.knowave.cashboard.domains.financialschedule.service.dto.CreateFinancialScheduleCommand
import com.knowave.cashboard.domains.financialschedule.service.dto.FinancialScheduleResult
import com.knowave.cashboard.domains.financialschedule.service.dto.PatchFinancialScheduleCommand
import java.util.UUID

interface FinancialScheduleService {
	fun create(userId: UUID, command: CreateFinancialScheduleCommand): FinancialScheduleResult
	fun get(userId: UUID, id: UUID): FinancialScheduleResult
	fun getAll(userId: UUID): List<FinancialScheduleResult>
	fun patch(userId: UUID, id: UUID, command: PatchFinancialScheduleCommand): FinancialScheduleResult
	fun delete(userId: UUID, id: UUID)
}
