package com.knowave.cashboard.domains.financialschedule.context

import com.knowave.cashboard.domains.account.entity.Account
import com.knowave.cashboard.domains.account.repository.AccountRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class RepositoryLiquidityBalanceProviderTest {
	private val userId = UUID.fromString("00000000-0000-0000-0000-0000000000aa")
	private val repository = FakeAccountRepository()
	private val provider = RepositoryLiquidityBalanceProvider(repository)

	@Test
	fun `LIQUID 계좌만 현재 잔액으로 합산한다`() {
		repository.accounts = listOf(
			Account(userId, "생활비", "LIQUID", 1_200_000L),
			Account(userId, "비상금", "EMERGENCY", 5_000_000L),
			Account(userId, "입출금", "liquid", 300_000L),
		)

		assertThat(provider.getCurrentLiquidBalance(userId)).isEqualTo(1_500_000L)
	}

	@Test
	fun `LIQUID 계좌가 없으면 0을 반환한다`() {
		repository.accounts = emptyList()

		assertThat(provider.getCurrentLiquidBalance(userId)).isZero()
	}
}

private class FakeAccountRepository : AccountRepository {
	var accounts: List<Account> = emptyList()

	override fun save(account: Account): Account = account
	override fun findByIdAndUserId(id: UUID, userId: UUID): Account? = null
	override fun findAllByUserId(userId: UUID): List<Account> = accounts
	override fun delete(account: Account) = Unit
}
