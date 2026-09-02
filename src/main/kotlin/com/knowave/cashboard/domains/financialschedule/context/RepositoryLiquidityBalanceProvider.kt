package com.knowave.cashboard.domains.financialschedule.context

import com.knowave.cashboard.domains.account.entity.AccountType
import com.knowave.cashboard.domains.account.repository.AccountRepository
import org.springframework.stereotype.Component

@Component
class RepositoryLiquidityBalanceProvider(
	private val accountRepository: AccountRepository,
) : LiquidityBalanceProvider {
	override fun getCurrentLiquidBalance(): Long = accountRepository.findAll()
		.filter { AccountType.from(it.type) == AccountType.LIQUID }
		.sumOf { it.balance }
}
