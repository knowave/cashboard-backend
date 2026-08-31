package com.knowave.cashboard.domains.simulation.service

import com.knowave.cashboard.common.exception.CashboardException
import com.knowave.cashboard.domains.fixedexpense.entity.FixedExpense
import com.knowave.cashboard.domains.fixedexpense.repository.FixedExpenseRepository
import com.knowave.cashboard.domains.loan.entity.Loan
import com.knowave.cashboard.domains.loan.repository.LoanRepository
import com.knowave.cashboard.domains.simulation.legacy.LegacyEarlyRepaymentAdapter
import com.knowave.cashboard.domains.simulation.service.dto.EarlyRepaymentSimulationCommand
import com.knowave.cashboard.domains.simulation.service.dto.EarlyRepaymentSimulationResult
import com.knowave.cashboard.domains.simulation.service.dto.MonthlyCashFlowResult
import com.knowave.cashboard.domains.simulation.service.dto.MonthlySimulationCommand
import org.springframework.stereotype.Service
import java.time.YearMonth

@Service
class SimulationServiceImpl(
	private val fixedExpenseRepository: FixedExpenseRepository,
	private val loanRepository: LoanRepository,
	private val legacyEarlyRepaymentAdapter: LegacyEarlyRepaymentAdapter,
) : SimulationService {
	override fun simulateMonthly(command: MonthlySimulationCommand): List<MonthlyCashFlowResult> {
		if (command.to.isBefore(command.from)) {
			throw CashboardException("INVALID_PERIOD", "to must be greater than or equal to from.")
		}

		val fixedExpenses = fixedExpenseRepository.findAll()
		val loans = loanRepository.findAll()
		var estimatedLoanBalance = loans.sumOf { it.currentBalance }
		val results = mutableListOf<MonthlyCashFlowResult>()
		var month = command.from

		while (!month.isAfter(command.to)) {
			val fixedExpense = fixedExpenses.filter { it.isActiveIn(month) }.sumOf { it.amount }
			val loanPayment = loans.filter { it.isActiveIn(month) }.sumOf { it.monthlyPayment }
			val availableLivingExpense = command.monthlySalary - fixedExpense - command.emergencyFund - command.savings
			val netCashFlow = availableLivingExpense - loanPayment
			estimatedLoanBalance = (estimatedLoanBalance - loanPayment).coerceAtLeast(0)
			results += MonthlyCashFlowResult(
				month = month.toString(),
				salary = command.monthlySalary,
				fixedExpense = fixedExpense,
				emergencyFund = command.emergencyFund,
				savings = command.savings,
				loanPayment = loanPayment,
				availableLivingExpense = availableLivingExpense,
				netCashFlow = netCashFlow,
				estimatedLoanBalance = estimatedLoanBalance,
			)
			month = month.plusMonths(1)
		}

		return results
	}

	override fun simulateEarlyRepayment(command: EarlyRepaymentSimulationCommand): EarlyRepaymentSimulationResult =
		legacyEarlyRepaymentAdapter.simulate(command)

	private fun FixedExpense.isActiveIn(month: YearMonth): Boolean {
		val start = YearMonth.parse(startMonth)
		val end = endMonth?.let { YearMonth.parse(it) }
		return !month.isBefore(start) && (end == null || !month.isAfter(end))
	}

	private fun Loan.isActiveIn(month: YearMonth): Boolean {
		val start = YearMonth.parse(startMonth)
		val maturity = YearMonth.parse(maturityMonth)
		return !month.isBefore(start) && !month.isAfter(maturity)
	}
}
