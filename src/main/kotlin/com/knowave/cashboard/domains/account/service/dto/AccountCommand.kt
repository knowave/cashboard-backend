package com.knowave.cashboard.domains.account.service.dto

import com.knowave.cashboard.domains.account.entity.Account
import com.knowave.cashboard.domains.account.entity.AccountType
import java.util.UUID

data class CreateAccountCommand(
	val name: String,
	val type: AccountType,
	val balance: Long,
) {
	fun toEntity(userId: UUID): Account = Account(
		userId = userId,
		name = name,
		type = type.name,
		balance = balance,
	)
}

data class UpdateAccountCommand(
	val name: String,
	val type: AccountType,
	val balance: Long,
)
