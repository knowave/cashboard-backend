package com.knowave.cashboard.domains.simulation.calculator

import com.knowave.cashboard.common.exception.InvalidLoanRepaymentConditionException
import com.knowave.cashboard.domains.simulation.context.LoanSnapshot
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

class LoanRepaymentCalculatorTest {
	private val calculator = LoanRepaymentCalculator()
	private val baseDate = LocalDate.of(2026, 8, 31)

	@Test
	fun `무이자 대출은 마지막 부분 납입까지 계산하고 조기상환으로 기간이 단축된다`() {
		val loan = loan(balance = 1_000L, rate = "0", payment = 300L)

		val result = calculator.calculate(loan, prepaymentAmount = 400L, baseDate = baseDate)

		assertThat(result.current.remainingMonths).isEqualTo(4)
		assertThat(result.current.estimatedTotalInterestAmount).isZero()
		assertThat(result.current.estimatedTotalPaymentAmount).isEqualTo(1_000L)
		assertThat(result.current.estimatedPayoffMonth).isEqualTo(YearMonth.of(2026, 12))
		assertThat(result.simulated.remainingPrincipalAmount).isEqualTo(600L)
		assertThat(result.simulated.remainingMonths).isEqualTo(2)
		assertThat(result.difference.reducedMonths).isEqualTo(2)
	}

	@Test
	fun `월 이자를 원 단위 HALF_UP으로 반올림해 이자 절감액을 계산한다`() {
		val loan = loan(balance = 1_200L, rate = "12.0", payment = 500L)

		val result = calculator.calculate(loan, prepaymentAmount = 500L, baseDate = baseDate)

		assertThat(result.current.remainingMonths).isEqualTo(3)
		assertThat(result.current.estimatedTotalInterestAmount).isEqualTo(21L)
		assertThat(result.simulated.remainingMonths).isEqualTo(2)
		assertThat(result.simulated.estimatedTotalInterestAmount).isEqualTo(9L)
		assertThat(result.difference.savedInterestAmount).isEqualTo(12L)
		assertThat(result.difference.reducedMonths).isEqualTo(1)
	}

	@Test
	fun `잔액 전액을 조기상환하면 납입 개월과 이자가 0이다`() {
		val result = calculator.calculate(
			loan = loan(balance = 1_200L, rate = "12.0", payment = 500L),
			prepaymentAmount = 1_200L,
			baseDate = baseDate,
		)

		assertThat(result.simulated.remainingPrincipalAmount).isZero()
		assertThat(result.simulated.remainingMonths).isZero()
		assertThat(result.simulated.estimatedTotalInterestAmount).isZero()
		assertThat(result.simulated.estimatedPayoffMonth).isEqualTo(YearMonth.of(2026, 8))
	}

	@Test
	fun `월 납입액이 첫 달 이자 이하이면 상환 불가능 조건으로 거절한다`() {
		val loan = loan(balance = 1_000_000L, rate = "12.0", payment = 10_000L)

		assertThatThrownBy { calculator.calculate(loan, 0L, baseDate) }
			.isInstanceOf(InvalidLoanRepaymentConditionException::class.java)
			.hasMessageContaining("monthly payment")
	}

	@Test
	fun `상환 기간이 1200개월을 넘으면 병적인 스케줄로 거절한다`() {
		val loan = loan(balance = 1_201L, rate = "0", payment = 1L)

		assertThatThrownBy { calculator.calculate(loan, 0L, baseDate) }
			.isInstanceOf(InvalidLoanRepaymentConditionException::class.java)
			.hasMessageContaining("1200")
	}

	private fun loan(balance: Long, rate: String, payment: Long) = LoanSnapshot(
		id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
		currentBalance = balance,
		annualInterestRate = BigDecimal(rate),
		monthlyPaymentAmount = payment,
	)
}
