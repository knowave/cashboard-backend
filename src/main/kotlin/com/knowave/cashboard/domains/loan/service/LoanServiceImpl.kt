package com.knowave.cashboard.domains.loan.service

import com.knowave.cashboard.common.exception.CashboardException
import com.knowave.cashboard.common.exception.NotFoundException
import com.knowave.cashboard.domains.loan.service.dto.CreateLoanCommand
import com.knowave.cashboard.domains.loan.service.dto.LoanResult
import com.knowave.cashboard.domains.loan.service.dto.UpdateLoanCommand
import com.knowave.cashboard.domains.loan.service.dto.toResult
import com.knowave.cashboard.domains.loan.repository.LoanRepository
import org.springframework.stereotype.Service
import java.time.YearMonth
import java.util.UUID

@Service
class LoanServiceImpl(
	private val loanRepository: LoanRepository,
) : LoanService {
	override fun create(command: CreateLoanCommand): LoanResult {
		validatePeriod(command.startMonth, command.maturityMonth)
		return loanRepository.save(command.toEntity()).toResult()
	}

	override fun get(id: UUID): LoanResult {
		val loan = loanRepository.findById(id) ?: throw NotFoundException("Loan", id)
		return loan.toResult()
	}

	override fun getAll(): List<LoanResult> = loanRepository.findAll().map { it.toResult() }

	override fun update(id: UUID, command: UpdateLoanCommand): LoanResult {
		validatePeriod(command.startMonth, command.maturityMonth)
		val loan = loanRepository.findById(id) ?: throw NotFoundException("Loan", id)

		loan.update(
			principal = command.principal,
			annualInterestRate = command.annualInterestRate,
			monthlyPayment = command.monthlyPayment,
			currentBalance = command.currentBalance,
			startMonth = command.startMonth,
			maturityMonth = command.maturityMonth,
		)
		return loanRepository.save(loan).toResult()
	}

	override fun delete(id: UUID) {
		val loan = loanRepository.findById(id) ?: throw NotFoundException("Loan", id)
		loanRepository.delete(loan)
	}

	private fun validatePeriod(startMonth: YearMonth, maturityMonth: YearMonth) {
		if (maturityMonth.isBefore(startMonth)) {
			throw CashboardException("INVALID_PERIOD", "maturityMonth must be greater than or equal to startMonth.")
		}
	}
}
