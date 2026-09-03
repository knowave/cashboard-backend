package com.knowave.cashboard.domains.notification.repository

import com.knowave.cashboard.domains.notification.entity.BalanceShortageState
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

interface BalanceShortageStateRepository {
	fun find(): BalanceShortageState?
	fun save(state: BalanceShortageState): BalanceShortageState
}

interface BalanceShortageStateJpaRepository : JpaRepository<BalanceShortageState, UUID> {
	fun findByScopeKey(scopeKey: String): BalanceShortageState?
}

@Repository
class BalanceShortageStateRepositoryImpl(
	private val balanceShortageStateJpaRepository: BalanceShortageStateJpaRepository,
) : BalanceShortageStateRepository {
	override fun find(): BalanceShortageState? = balanceShortageStateJpaRepository.findByScopeKey("SINGLE_USER")

	override fun save(state: BalanceShortageState): BalanceShortageState = balanceShortageStateJpaRepository.save(state)
}
