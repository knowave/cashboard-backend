package com.knowave.cashboard.domains.fixedexpense.service

import com.knowave.cashboard.domains.fixedexpense.service.dto.CreateFixedExpenseCommand
import com.knowave.cashboard.domains.fixedexpense.service.dto.FixedExpenseResult
import com.knowave.cashboard.domains.fixedexpense.service.dto.UpdateFixedExpenseCommand
import java.util.UUID

interface FixedExpenseService {
	fun create(command: CreateFixedExpenseCommand): FixedExpenseResult
	fun get(id: UUID): FixedExpenseResult
	fun getAll(): List<FixedExpenseResult>
	fun update(id: UUID, command: UpdateFixedExpenseCommand): FixedExpenseResult
	fun delete(id: UUID)
}
