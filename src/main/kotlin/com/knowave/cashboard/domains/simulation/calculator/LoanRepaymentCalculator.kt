package com.knowave.cashboard.domains.simulation.calculator

import com.knowave.cashboard.common.exception.InvalidLoanRepaymentConditionException
import com.knowave.cashboard.domains.simulation.context.LoanSnapshot
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.YearMonth

data class LoanRepaymentSchedule(
	val remainingPrincipalAmount: Long,
	val monthlyPaymentAmount: Long,
	val remainingMonths: Int,
	val estimatedTotalInterestAmount: Long,
	val estimatedTotalPaymentAmount: Long,
	val estimatedPayoffMonth: YearMonth,
)

data class LoanRepaymentDifference(
	val savedInterestAmount: Long,
	val reducedMonths: Int,
)

data class LoanRepaymentComparison(
	val current: LoanRepaymentSchedule,
	val simulated: LoanRepaymentSchedule,
	val difference: LoanRepaymentDifference,
)

class LoanRepaymentCalculator {
	fun calculate(
		loan: LoanSnapshot,
		prepaymentAmount: Long,
		baseDate: LocalDate,
	): LoanRepaymentComparison {
		require(prepaymentAmount in 0L..loan.currentBalance)

		val current = buildSchedule(loan.currentBalance, loan, baseDate)
		val simulated = buildSchedule(loan.currentBalance - prepaymentAmount, loan, baseDate)

		return LoanRepaymentComparison(
			current = current,
			simulated = simulated,
			difference = LoanRepaymentDifference(
				savedInterestAmount = (current.estimatedTotalInterestAmount -
					simulated.estimatedTotalInterestAmount).coerceAtLeast(0L),
				reducedMonths = (current.remainingMonths - simulated.remainingMonths).coerceAtLeast(0),
			),
		)
	}

	private fun buildSchedule(
		startingBalance: Long,
		loan: LoanSnapshot,
		baseDate: LocalDate,
	): LoanRepaymentSchedule {
		if (startingBalance == 0L) {
			return LoanRepaymentSchedule(
				remainingPrincipalAmount = 0L,
				monthlyPaymentAmount = loan.monthlyPaymentAmount,
				remainingMonths = 0,
				estimatedTotalInterestAmount = 0L,
				estimatedTotalPaymentAmount = 0L,
				estimatedPayoffMonth = YearMonth.from(baseDate),
			)
		}

		var balance = startingBalance
		var totalInterest = 0L
		var months = 0

		while (balance > 0L) {
			if (months >= MAX_INSTALLMENTS) {
				throw InvalidLoanRepaymentConditionException(
					"Loan repayment schedule exceeds $MAX_INSTALLMENTS monthly installments.",
				)
			}

			val interest = monthlyInterest(balance, loan.annualInterestRate)
			if (loan.monthlyPaymentAmount <= interest) {
				throw InvalidLoanRepaymentConditionException(
					"Loan monthly payment must be greater than monthly interest.",
				)
			}

			val principalPayment = minOf(balance, loan.monthlyPaymentAmount - interest)
			balance -= principalPayment
			totalInterest += interest
			months += 1
		}

		return LoanRepaymentSchedule(
			remainingPrincipalAmount = startingBalance,
			monthlyPaymentAmount = loan.monthlyPaymentAmount,
			remainingMonths = months,
			estimatedTotalInterestAmount = totalInterest,
			estimatedTotalPaymentAmount = startingBalance + totalInterest,
			estimatedPayoffMonth = YearMonth.from(baseDate).plusMonths(months.toLong()),
		)
	}

	private fun monthlyInterest(balance: Long, annualInterestRate: BigDecimal): Long =
		BigDecimal.valueOf(balance)
			.multiply(annualInterestRate)
			.divide(BigDecimal("1200"), 12, RoundingMode.HALF_UP)
			.setScale(0, RoundingMode.HALF_UP)
			.longValueExact()

	private companion object {
		const val MAX_INSTALLMENTS = 1_200
	}
}
