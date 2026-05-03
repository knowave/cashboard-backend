package com.knowave.cashboard.domains.account.service

import com.knowave.cashboard.common.exception.NotFoundException
import com.knowave.cashboard.domains.account.service.dto.AccountResult
import com.knowave.cashboard.domains.account.service.dto.CreateAccountCommand
import com.knowave.cashboard.domains.account.service.dto.UpdateAccountCommand
import com.knowave.cashboard.domains.account.service.dto.toResult
import com.knowave.cashboard.domains.account.repository.AccountRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AccountServiceImpl(
	private val accountRepository: AccountRepository,
) : AccountService {
	override fun create(command: CreateAccountCommand): AccountResult =
		accountRepository.save(command.toEntity()).toResult()

	override fun getAccount(id: UUID): AccountResult {
		val account = accountRepository.findById(id) ?: throw NotFoundException("Account", id)
		return account.toResult()
	}

	override fun getAllAccount(): List<AccountResult> = accountRepository.findAll().map { it.toResult() }

	override fun update(id: UUID, command: UpdateAccountCommand): AccountResult {
		val account = accountRepository.findById(id) ?: throw NotFoundException("Account", id)
		account.update(command.name, command.type, command.balance)
		return accountRepository.save(account).toResult()
	}

	override fun delete(id: UUID) {
		val account = accountRepository.findById(id) ?: throw NotFoundException("Account", id)
		accountRepository.delete(account)
	}
}
