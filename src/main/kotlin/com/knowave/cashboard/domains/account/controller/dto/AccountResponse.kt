package com.knowave.cashboard.domains.account.controller.dto

import com.knowave.cashboard.domains.account.service.dto.AccountResult
import java.time.LocalDateTime
import java.util.UUID

data class AccountResponse(
	val id: UUID,
	val name: String,
	val type: String,
	val balance: Long,
	val createdAt: LocalDateTime?,
	val updatedAt: LocalDateTime?,
)

fun AccountResult.toResponse(): AccountResponse = AccountResponse(
	id = id,
	name = name,
	type = type,
	balance = balance,
	createdAt = createdAt,
	updatedAt = updatedAt,
)
