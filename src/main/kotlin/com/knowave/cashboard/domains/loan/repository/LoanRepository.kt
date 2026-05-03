package com.knowave.cashboard.domains.loan.repository

import com.knowave.cashboard.domains.loan.entity.Loan
import java.util.UUID

interface LoanRepository {
	fun save(loan: Loan): Loan
	fun findById(id: UUID): Loan?
	fun findAll(): List<Loan>
	fun delete(loan: Loan)
}
