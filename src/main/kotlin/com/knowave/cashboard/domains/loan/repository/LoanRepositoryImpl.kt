package com.knowave.cashboard.domains.loan.repository

import com.knowave.cashboard.domains.loan.entity.Loan
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class LoanRepositoryImpl(
	private val loanJpaRepository: LoanJpaRepository,
) : LoanRepository {
	override fun save(loan: Loan): Loan = loanJpaRepository.save(loan)

	override fun findByIdAndUserId(id: UUID, userId: UUID): Loan? = loanJpaRepository.findByIdAndUserId(id, userId)

	override fun findAllByUserId(userId: UUID): List<Loan> = loanJpaRepository.findAllByUserId(userId)

	override fun delete(loan: Loan) = loanJpaRepository.delete(loan)
}
