package com.knowave.cashboard.domains.account.repository

import com.knowave.cashboard.domains.account.entity.Account
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class AccountRepositoryImpl(
	private val accountJpaRepository: AccountJpaRepository,
) : AccountRepository {
	override fun save(account: Account): Account = accountJpaRepository.save(account)

	override fun findById(id: UUID): Account? = accountJpaRepository.findById(id).orElse(null)

	override fun findAll(): List<Account> = accountJpaRepository.findAll()

	override fun delete(account: Account) = accountJpaRepository.delete(account)
}
