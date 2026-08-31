package com.knowave.cashboard.domains.simulation.context

import com.knowave.cashboard.common.exception.NotFoundException
import com.knowave.cashboard.domains.account.entity.AccountType
import com.knowave.cashboard.domains.account.repository.AccountRepository
import com.knowave.cashboard.domains.expenseanalysis.repository.ExpenseAnalysisRepository
import com.knowave.cashboard.domains.loan.repository.LoanRepository
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

@Component
class RepositorySimulationContextProvider(
	private val accountRepository: AccountRepository,
	private val expenseAnalysisRepository: ExpenseAnalysisRepository,
	private val loanRepository: LoanRepository,
	private val clock: Clock,
) : SimulationContextProvider {
	override fun loadLiquidityContext(): LiquidityContext {
		val baseDate = LocalDate.now(clock)
		val accounts = accountRepository.findAll()
		val end = YearMonth.from(baseDate).atDay(1)
		val start = YearMonth.from(baseDate).minusMonths(EXPENSE_LOOKBACK_MONTHS).atDay(1)
		val expenses = expenseAnalysisRepository.findMonthlyExpenses(start, end)

		return LiquidityContext(
			baseDate = baseDate,
			liquidAssetAmount = accounts
				.filter { AccountType.from(it.type) == AccountType.LIQUID }
				.sumOf { it.balance },
			emergencyAssetAmount = accounts
				.filter { AccountType.from(it.type) == AccountType.EMERGENCY }
				.sumOf { it.balance },
			averageMonthlyExpenseAmount = if (expenses.isEmpty()) 0L else expenses.sumOf { it.amount } / expenses.size,
			expenseHistoryMonthCount = expenses.size,
		)
	}

	override fun loadLoanRepaymentContext(loanId: UUID): SimulationContext {
		val liquidity = loadLiquidityContext()
		val loan = loanRepository.findById(loanId) ?: throw NotFoundException("Loan", loanId)
		return SimulationContext(
			baseDate = liquidity.baseDate,
			liquidAssetAmount = liquidity.liquidAssetAmount,
			emergencyAssetAmount = liquidity.emergencyAssetAmount,
			averageMonthlyExpenseAmount = liquidity.averageMonthlyExpenseAmount,
			expenseHistoryMonthCount = liquidity.expenseHistoryMonthCount,
			loan = LoanSnapshot(
				id = requireNotNull(loan.id),
				currentBalance = loan.currentBalance,
				annualInterestRate = loan.annualInterestRate,
				monthlyPaymentAmount = loan.monthlyPayment,
			),
		)
	}

	private companion object {
		const val EXPENSE_LOOKBACK_MONTHS = 3L
	}
}
