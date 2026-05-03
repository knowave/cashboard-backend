package com.knowave.cashboard.domains.account.service

import com.knowave.cashboard.domains.account.service.dto.AccountResult
import com.knowave.cashboard.domains.account.service.dto.CreateAccountCommand
import com.knowave.cashboard.domains.account.service.dto.UpdateAccountCommand
import java.util.UUID

interface AccountService {
	fun create(command: CreateAccountCommand): AccountResult
	fun getAccount(id: UUID): AccountResult
	fun getAllAccount(): List<AccountResult>
	fun update(id: UUID, command: UpdateAccountCommand): AccountResult
	fun delete(id: UUID)
}
