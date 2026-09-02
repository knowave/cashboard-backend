package com.knowave.cashboard.domains.financialschedule.context

import com.knowave.cashboard.domains.account.entity.Account
import com.knowave.cashboard.domains.account.repository.AccountRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class RepositoryLiquidityBalanceProviderTest {
	private val repository = FakeAccountRepository()
	private val provider = RepositoryLiquidityBalanceProvider(repository)

	@Test
	fun `LIQUID 계좌만 현재 잔액으로 합산한다`() {
		repository.accounts = listOf(
			Account("생활비", "LIQUID", 1_200_000L),
			Account("비상금", "EMERGENCY", 5_000_000L),
			Account("입출금", "liquid", 300_000L),
		)

		assertThat(provider.getCurrentLiquidBalance()).isEqualTo(1_500_000L)
	}

	@Test
	fun `LIQUID 계좌가 없으면 0을 반환한다`() {
		repository.accounts = emptyList()

		assertThat(provider.getCurrentLiquidBalance()).isZero()
	}
}

private class FakeAccountRepository : AccountRepository {
	var accounts: List<Account> = emptyList()

	override fun save(account: Account): Account = account
	override fun findById(id: UUID): Account? = null
	override fun findAll(): List<Account> = accounts
	override fun delete(account: Account) = Unit
}
