package com.knowave.cashboard.domains.loan.repository

import com.knowave.cashboard.domains.loan.entity.Loan
import java.util.UUID

interface LoanRepository {
	fun save(loan: Loan): Loan
	fun findByIdAndUserId(id: UUID, userId: UUID): Loan?
	fun findAllByUserId(userId: UUID): List<Loan>
	fun delete(loan: Loan)
}
