package com.knowave.cashboard.domains.account.service.dto

import com.knowave.cashboard.domains.account.entity.Account
import java.time.LocalDateTime
import java.util.UUID

data class AccountResult(
	val id: UUID,
	val name: String,
	val type: String,
	val balance: Long,
	val createdAt: LocalDateTime?,
	val updatedAt: LocalDateTime?,
)

fun Account.toResult(): AccountResult = AccountResult(
	id = requireNotNull(id),
	name = name,
	type = type,
	balance = balance,
	createdAt = createdAt,
	updatedAt = updatedAt,
)
