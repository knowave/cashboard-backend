package com.knowave.cashboard.domains.account.service

import com.knowave.cashboard.common.entity.BaseEntity
import com.knowave.cashboard.domains.account.entity.Account
import com.knowave.cashboard.domains.account.entity.AccountType
import com.knowave.cashboard.domains.account.repository.AccountRepository
import com.knowave.cashboard.domains.account.repository.AccountBalanceLockRepository
import com.knowave.cashboard.domains.account.service.dto.CreateAccountCommand
import com.knowave.cashboard.domains.account.service.dto.UpdateAccountCommand
import com.knowave.cashboard.domains.assetgoal.event.TotalAssetAmountChangedEvent
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

class AccountServiceImplTest {
	private val userId = UUID.fromString("00000000-0000-0000-0000-0000000000aa")
	private val operations = mutableListOf<String>()
	private val repository = InMemoryAccountRepository(operations)
	private val eventPublisher = RecordingEventPublisher()
	private val balanceLockRepository = RecordingAccountBalanceLockRepository(operations)
	private val fixedInstant = Instant.parse("2026-09-02T00:00:00Z")
	private val service = AccountServiceImpl(
		accountRepository = repository,
		accountBalanceLockRepository = balanceLockRepository,
		eventPublisher = eventPublisher,
		clock = Clock.fixed(fixedInstant, ZoneOffset.UTC),
	)

	@Test
	fun `계좌 생성은 저장 전후 총자산 변경 이벤트를 발행한다`() {
		repository.save(Account(userId, "현금", "LIQUID", 40L))
		operations.clear()

		service.create(userId, CreateAccountCommand("예금", AccountType.LIQUID, 60L))

		assertThat(eventPublisher.events).containsExactly(
			TotalAssetAmountChangedEvent(userId, 40L, 100L, fixedInstant),
		)
		assertThat(operations).containsExactly("lock", "findAll", "save")
	}

	@Test
	fun `계좌 수정은 전체 자산 변경 이벤트를 발행한다`() {
		val accountId = UUID.fromString("00000000-0000-0000-0000-000000000001")
		repository.save(Account(userId, "현금", "LIQUID", 40L).also { it.assignBaseFields(accountId) })
		operations.clear()

		service.update(userId, accountId, UpdateAccountCommand("예금", AccountType.LIQUID, 100L))

		assertThat(eventPublisher.events).containsExactly(
			TotalAssetAmountChangedEvent(userId, 40L, 100L, fixedInstant),
		)
		assertThat(operations).containsExactly("lock", "findAll", "findById", "save")
	}

	@Test
	fun `계좌 삭제는 총자산 변경 이벤트를 발행하지 않는다`() {
		val accountId = UUID.fromString("00000000-0000-0000-0000-000000000001")
		repository.save(Account(userId, "현금", "LIQUID", 40L).also { it.assignBaseFields(accountId) })
		operations.clear()

		service.delete(userId, accountId)

		assertThat(eventPublisher.events).isEmpty()
		assertThat(operations).containsExactly("lock", "findById", "delete")
	}

	@Test
	fun `계좌 생성은 총자산 합계 overflow를 감지한다`() {
		repository.save(Account(userId, "현금", "LIQUID", Long.MAX_VALUE))

		assertThatThrownBy { service.create(userId, CreateAccountCommand("예금", AccountType.LIQUID, 1L)) }
			.isInstanceOf(ArithmeticException::class.java)
		assertThat(eventPublisher.events).isEmpty()
	}

	@Test
	fun `계좌 수정은 변경 전 잔액 차감 overflow를 감지한다`() {
		val accountId = UUID.fromString("00000000-0000-0000-0000-000000000001")
		repository.snapshot = listOf(Account(userId, "합계", "LIQUID", Long.MIN_VALUE))
		repository.save(Account(userId, "현금", "LIQUID", 1L).also { it.assignBaseFields(accountId) })

		assertThatThrownBy { service.update(userId, accountId, UpdateAccountCommand("현금", AccountType.LIQUID, 2L)) }
			.isInstanceOf(ArithmeticException::class.java)
		assertThat(eventPublisher.events).isEmpty()
	}
}

private class InMemoryAccountRepository(
	private val operations: MutableList<String>,
) : AccountRepository {
	private val accounts = linkedMapOf<UUID, Account>()
	var snapshot: List<Account>? = null

	override fun save(account: Account): Account {
		operations += "save"
		if (account.id == null) account.assignBaseFields()
		accounts[requireNotNull(account.id)] = account
		return account
	}

	override fun findByIdAndUserId(id: UUID, userId: UUID): Account? {
		operations += "findById"
		return accounts[id]?.takeIf { it.userId == userId }
	}

	override fun findAllByUserId(userId: UUID): List<Account> {
		operations += "findAll"
		return snapshot ?: accounts.values.filter { it.userId == userId }
	}

	override fun delete(account: Account) {
		operations += "delete"
		accounts.remove(account.id)
	}
}

private class RecordingAccountBalanceLockRepository(
	private val operations: MutableList<String>,
) : AccountBalanceLockRepository {

	override fun acquireTotalAssetLock(userId: UUID) {
		operations += "lock"
	}
}

private class RecordingEventPublisher : ApplicationEventPublisher {
	val events = mutableListOf<Any>()
	override fun publishEvent(event: Any) { events += event }
}

private fun BaseEntity.assignBaseFields(id: UUID = UUID.randomUUID()) {
	val baseClass = BaseEntity::class.java
	baseClass.getDeclaredField("id").apply { isAccessible = true; set(this@assignBaseFields, id) }
	baseClass.getDeclaredField("createdAt").apply { isAccessible = true; set(this@assignBaseFields, LocalDateTime.now()) }
	baseClass.getDeclaredField("updatedAt").apply { isAccessible = true; set(this@assignBaseFields, LocalDateTime.now()) }
}
