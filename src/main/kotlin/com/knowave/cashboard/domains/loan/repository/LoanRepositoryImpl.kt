package com.knowave.cashboard.domains.loan.repository

import com.knowave.cashboard.domains.loan.entity.Loan
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class LoanRepositoryImpl(
	private val loanJpaRepository: LoanJpaRepository,
) : LoanRepository {
	override fun save(loan: Loan): Loan = loanJpaRepository.save(loan)

	override fun findById(id: UUID): Loan? = loanJpaRepository.findById(id).orElse(null)

	override fun findAll(): List<Loan> = loanJpaRepository.findAll()

	override fun delete(loan: Loan) = loanJpaRepository.delete(loan)
}
