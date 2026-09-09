package com.knowave.cashboard.domains.account.repository

import com.knowave.cashboard.domains.account.entity.Account
import java.util.UUID

interface AccountRepository {
	fun save(account: Account): Account
	fun findByIdAndUserId(id: UUID, userId: UUID): Account?
	fun findAllByUserId(userId: UUID): List<Account>
	fun delete(account: Account)
}
