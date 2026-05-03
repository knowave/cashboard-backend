package com.knowave.cashboard.domains.loan.service

import com.knowave.cashboard.domains.loan.service.dto.CreateLoanCommand
import com.knowave.cashboard.domains.loan.service.dto.LoanResult
import com.knowave.cashboard.domains.loan.service.dto.UpdateLoanCommand
import java.util.UUID

interface LoanService {
	fun create(command: CreateLoanCommand): LoanResult
	fun get(id: UUID): LoanResult
	fun getAll(): List<LoanResult>
	fun update(id: UUID, command: UpdateLoanCommand): LoanResult
	fun delete(id: UUID)
}
