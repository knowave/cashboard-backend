package com.knowave.cashboard.domains.simulation.context

import com.knowave.cashboard.common.entity.BaseEntity
import com.knowave.cashboard.common.exception.NotFoundException
import com.knowave.cashboard.domains.account.entity.Account
import com.knowave.cashboard.domains.account.repository.AccountRepository
import com.knowave.cashboard.domains.expenseanalysis.repository.ExpenseAnalysisRepository
import com.knowave.cashboard.domains.expenseanalysis.repository.dto.MonthlyExpenseProjection
import com.knowave.cashboard.domains.loan.entity.Loan
import com.knowave.cashboard.domains.loan.repository.LoanRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class RepositorySimulationContextProviderTest {
	@Mock private lateinit var accountRepository: AccountRepository
	@Mock private lateinit var expenseRepository: ExpenseAnalysisRepository
	@Mock private lateinit var loanRepository: LoanRepository

	private lateinit var provider: RepositorySimulationContextProvider
	private val loanId = UUID.fromString("00000000-0000-0000-0000-000000000001")

	@BeforeEach
	fun setUp() {
		val clock = Clock.fixed(Instant.parse("2026-08-31T03:00:00Z"), ZoneId.of("Asia/Seoul"))
		provider = RepositorySimulationContextProvider(accountRepository, expenseRepository, loanRepository, clock)
	}

	@Test
	fun `계좌와 직전 완료 3개월 지출을 하나의 대출 시뮬레이션 Context로 만든다`() {
		given(accountRepository.findAll()).willReturn(
			listOf(
				Account("생활비", "LIQUID", 5_000_000L),
				Account("비상금", "EMERGENCY", 3_000_000L),
				Account("투자", "INVESTMENT", 9_000_000L),
			),
		)
		given(expenseRepository.findMonthlyExpenses(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 8, 1)))
			.willReturn(listOf(expense("2026-06", 1_200_000L), expense("2026-07", 1_600_000L)))
		given(loanRepository.findById(loanId)).willReturn(loan(loanId))

		val result = provider.loadLoanRepaymentContext(loanId)

		assertThat(result.baseDate).isEqualTo(LocalDate.of(2026, 8, 31))
		assertThat(result.liquidAssetAmount).isEqualTo(5_000_000L)
		assertThat(result.emergencyAssetAmount).isEqualTo(3_000_000L)
		assertThat(result.averageMonthlyExpenseAmount).isEqualTo(1_400_000L)
		assertThat(result.expenseHistoryMonthCount).isEqualTo(2)
		assertThat(result.loan.currentBalance).isEqualTo(26_000_000L)
		assertThat(result.loan.monthlyPaymentAmount).isEqualTo(500_000L)
		verify(expenseRepository).findMonthlyExpenses(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 8, 1))
	}

	@Test
	fun `지출 이력이 없으면 평균 0과 이력 개수 0을 반환한다`() {
		given(accountRepository.findAll()).willReturn(emptyList())
		given(expenseRepository.findMonthlyExpenses(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 8, 1)))
			.willReturn(emptyList())

		val result = provider.loadLiquidityContext()

		assertThat(result.averageMonthlyExpenseAmount).isZero()
		assertThat(result.expenseHistoryMonthCount).isZero()
	}

	@Test
	fun `대출이 없으면 NotFoundException을 던진다`() {
		given(accountRepository.findAll()).willReturn(emptyList())
		given(expenseRepository.findMonthlyExpenses(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 8, 1)))
			.willReturn(emptyList())
		given(loanRepository.findById(loanId)).willReturn(null)

		assertThatThrownBy { provider.loadLoanRepaymentContext(loanId) }
			.isInstanceOf(NotFoundException::class.java)
	}

	private fun expense(month: String, value: Long) = object : MonthlyExpenseProjection {
		override val yearMonth: String = month
		override val amount: Long = value
	}

	private fun loan(id: UUID): Loan = Loan(
		principal = 30_000_000L,
		annualInterestRate = BigDecimal("5.0"),
		monthlyPayment = 500_000L,
		currentBalance = 26_000_000L,
		startMonth = "2025-01",
		maturityMonth = "2031-01",
	).also { entity ->
		BaseEntity::class.java.getDeclaredField("id").apply {
			isAccessible = true
			set(entity, id)
		}
	}
}
