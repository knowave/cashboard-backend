package com.knowave.cashboard.domains.loan.service

import com.knowave.cashboard.domains.loan.service.dto.CreateLoanCommand
import com.knowave.cashboard.domains.loan.service.dto.LoanResult
import com.knowave.cashboard.domains.loan.service.dto.UpdateLoanCommand
import java.util.UUID

interface LoanService {
	fun create(userId: UUID, command: CreateLoanCommand): LoanResult
	fun get(userId: UUID, id: UUID): LoanResult
	fun getAll(userId: UUID): List<LoanResult>
	fun update(userId: UUID, id: UUID, command: UpdateLoanCommand): LoanResult
	fun delete(userId: UUID, id: UUID)
}
