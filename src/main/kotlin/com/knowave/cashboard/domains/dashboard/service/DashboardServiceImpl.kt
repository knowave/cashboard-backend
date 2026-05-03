package com.knowave.cashboard.domains.dashboard.service

import com.knowave.cashboard.domains.account.entity.AccountType
import com.knowave.cashboard.domains.account.repository.AccountRepository
import com.knowave.cashboard.domains.dashboard.service.dto.DashboardResult
import com.knowave.cashboard.domains.fixedexpense.repository.FixedExpenseRepository
import com.knowave.cashboard.domains.loan.repository.LoanRepository
import com.knowave.cashboard.domains.simulation.entity.EarlyRepaymentDecision
import org.springframework.stereotype.Service

@Service
class DashboardServiceImpl(
	private val accountRepository: AccountRepository,
	private val fixedExpenseRepository: FixedExpenseRepository,
	private val loanRepository: LoanRepository,
) : DashboardService {
	override fun getDashboard(): DashboardResult {
		val accounts = accountRepository.findAll()
		val loans = loanRepository.findAll()

        val liquidCash = accounts
			.filter { AccountType.from(it.type) == AccountType.LIQUID }
			.sumOf { it.balance }

        val emergencyBalance = accounts
			.filter { AccountType.from(it.type) == AccountType.EMERGENCY }
			.sumOf { it.balance }

        val savingsBalance = accounts
			.filter { AccountType.from(it.type) == AccountType.LACKED }
			.sumOf { it.balance }

        val investmentBalance = accounts
			.filter { AccountType.from(it.type) == AccountType.INVESTMENT }
			.sumOf { it.balance }

        val totalAccountBalance = accounts.sumOf { it.balance }
		val totalLoanBalance = loans.sumOf { it.currentBalance }
		val monthlyFixedExpense = fixedExpenseRepository.findAll().sumOf { it.amount }
		val monthlyLoanPayment = loans.sumOf { it.monthlyPayment }
		val possibleAmount = liquidCash - EMERGENCY_RESERVE_THRESHOLD
		val decision = EarlyRepaymentDecision.fromAvailableAmount(possibleAmount)

		return DashboardResult(
			totalAccountBalance = totalAccountBalance,
			liquidCash = liquidCash,
			emergencyBalance = emergencyBalance,
			savingsBalance = savingsBalance,
			investmentBalance = investmentBalance,
			totalLoanBalance = totalLoanBalance,
			monthlyFixedExpense = monthlyFixedExpense,
			monthlyLoanPayment = monthlyLoanPayment,
			netWorth = totalAccountBalance - totalLoanBalance,
			earlyRepaymentPossibleAmount = possibleAmount.coerceAtLeast(0),
			earlyRepaymentDecision = decision.name,
			earlyRepaymentDecisionDescription = decision.description,
		)
	}

	private companion object {
		const val EMERGENCY_RESERVE_THRESHOLD = 5_000_000L
	}
}
