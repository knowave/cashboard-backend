package com.knowave.cashboard.domains.account.repository

import com.knowave.cashboard.domains.account.entity.Account
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class AccountRepositoryImpl(
	private val accountJpaRepository: AccountJpaRepository,
) : AccountRepository {
	override fun save(account: Account): Account = accountJpaRepository.save(account)

	override fun findByIdAndUserId(id: UUID, userId: UUID): Account? =
		accountJpaRepository.findByIdAndUserId(id, userId)

	override fun findAllByUserId(userId: UUID): List<Account> = accountJpaRepository.findAllByUserId(userId)

	override fun delete(account: Account) = accountJpaRepository.delete(account)
}
