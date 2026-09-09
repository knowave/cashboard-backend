package com.knowave.cashboard.domains.account.service

import com.knowave.cashboard.domains.account.service.dto.AccountResult
import com.knowave.cashboard.domains.account.service.dto.CreateAccountCommand
import com.knowave.cashboard.domains.account.service.dto.UpdateAccountCommand
import java.util.UUID

interface AccountService {
	fun create(userId: UUID, command: CreateAccountCommand): AccountResult
	fun getAccount(userId: UUID, id: UUID): AccountResult
	fun getAllAccount(userId: UUID): List<AccountResult>
	fun update(userId: UUID, id: UUID, command: UpdateAccountCommand): AccountResult
	fun delete(userId: UUID, id: UUID)
}
