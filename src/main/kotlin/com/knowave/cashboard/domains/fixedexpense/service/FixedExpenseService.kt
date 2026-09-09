package com.knowave.cashboard.domains.fixedexpense.service

import com.knowave.cashboard.domains.fixedexpense.service.dto.CreateFixedExpenseCommand
import com.knowave.cashboard.domains.fixedexpense.service.dto.FixedExpenseResult
import com.knowave.cashboard.domains.fixedexpense.service.dto.UpdateFixedExpenseCommand
import java.util.UUID

interface FixedExpenseService {
	fun create(userId: UUID, command: CreateFixedExpenseCommand): FixedExpenseResult
	fun get(userId: UUID, id: UUID): FixedExpenseResult
	fun getAll(userId: UUID): List<FixedExpenseResult>
	fun update(userId: UUID, id: UUID, command: UpdateFixedExpenseCommand): FixedExpenseResult
	fun delete(userId: UUID, id: UUID)
}
