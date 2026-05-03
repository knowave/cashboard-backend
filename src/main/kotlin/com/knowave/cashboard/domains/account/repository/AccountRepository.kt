package com.knowave.cashboard.domains.account.repository

import com.knowave.cashboard.domains.account.entity.Account
import java.util.UUID

interface AccountRepository {
	fun save(account: Account): Account
	fun findById(id: UUID): Account?
	fun findAll(): List<Account>
	fun delete(account: Account)
}
