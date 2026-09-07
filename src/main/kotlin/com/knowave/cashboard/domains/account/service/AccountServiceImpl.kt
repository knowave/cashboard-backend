package com.knowave.cashboard.domains.account.service

import com.knowave.cashboard.common.exception.NotFoundException
import com.knowave.cashboard.domains.account.service.dto.AccountResult
import com.knowave.cashboard.domains.account.service.dto.CreateAccountCommand
import com.knowave.cashboard.domains.account.service.dto.UpdateAccountCommand
import com.knowave.cashboard.domains.account.service.dto.toResult
import com.knowave.cashboard.domains.account.repository.AccountRepository
import com.knowave.cashboard.domains.account.repository.AccountBalanceLockRepository
import com.knowave.cashboard.domains.assetgoal.event.TotalAssetAmountChangedEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.util.UUID

@Service
class AccountServiceImpl(
	private val accountRepository: AccountRepository,
	private val accountBalanceLockRepository: AccountBalanceLockRepository,
	private val eventPublisher: ApplicationEventPublisher,
	private val clock: Clock,
) : AccountService {
	@Transactional
	override fun create(command: CreateAccountCommand): AccountResult {
		accountBalanceLockRepository.acquireTotalAssetLock()
		val previousTotal = calculateTotalAmount()
		val saved = accountRepository.save(command.toEntity())
		val currentTotal = Math.addExact(previousTotal, saved.balance)
		eventPublisher.publishEvent(TotalAssetAmountChangedEvent(previousTotal, currentTotal, clock.instant()))
		return saved.toResult()
	}

	override fun getAccount(id: UUID): AccountResult {
		val account = accountRepository.findById(id) ?: throw NotFoundException("Account", id)
		return account.toResult()
	}

	override fun getAllAccount(): List<AccountResult> = accountRepository.findAll().map { it.toResult() }

	@Transactional
	override fun update(id: UUID, command: UpdateAccountCommand): AccountResult {
		accountBalanceLockRepository.acquireTotalAssetLock()
		val previousTotal = calculateTotalAmount()
		val account = accountRepository.findById(id) ?: throw NotFoundException("Account", id)
		val previousBalance = account.balance
		account.update(command.name, command.type, command.balance)
		val saved = accountRepository.save(account)
		val currentTotal = Math.addExact(Math.subtractExact(previousTotal, previousBalance), saved.balance)
		eventPublisher.publishEvent(TotalAssetAmountChangedEvent(previousTotal, currentTotal, clock.instant()))
		return saved.toResult()
	}

	@Transactional
	override fun delete(id: UUID) {
		accountBalanceLockRepository.acquireTotalAssetLock()
		val account = accountRepository.findById(id) ?: throw NotFoundException("Account", id)
		accountRepository.delete(account)
	}

	private fun calculateTotalAmount(): Long = accountRepository.findAll()
		.fold(0L) { total, account -> Math.addExact(total, account.balance) }
}
