package com.knowave.cashboard.domains.notification.repository

import com.knowave.cashboard.domains.notification.entity.BalanceShortageState
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID

data class BalanceShortageTransition(
	val episode: Long,
	val newEpisode: Boolean,
)

interface BalanceShortageStateRepository {
	fun findByScopeKeyForUpdate(scopeKey: String): BalanceShortageState?
	fun save(state: BalanceShortageState): BalanceShortageState
	fun transition(scopeKey: String, shortageDate: LocalDate?): BalanceShortageTransition
}

interface BalanceShortageStateJpaRepository : JpaRepository<BalanceShortageState, UUID> {
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	fun findByScopeKey(scopeKey: String): BalanceShortageState?

	@Modifying
	@Query(
		value = """
			INSERT INTO balance_shortage_states(id, scope_key, shortage_date, episode, created_at, updated_at)
			VALUES (:id, :scopeKey, NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
			ON CONFLICT (scope_key) DO NOTHING
		""",
		nativeQuery = true,
	)
	fun insertInitialIfAbsent(@Param("id") id: UUID, @Param("scopeKey") scopeKey: String): Int
}

@Repository
class BalanceShortageStateRepositoryImpl(
	private val balanceShortageStateJpaRepository: BalanceShortageStateJpaRepository,
) : BalanceShortageStateRepository {
	override fun findByScopeKeyForUpdate(scopeKey: String): BalanceShortageState? =
		balanceShortageStateJpaRepository.findByScopeKey(scopeKey)

	override fun save(state: BalanceShortageState): BalanceShortageState = balanceShortageStateJpaRepository.save(state)

	@Transactional
	override fun transition(scopeKey: String, shortageDate: LocalDate?): BalanceShortageTransition {
		balanceShortageStateJpaRepository.insertInitialIfAbsent(UUID.randomUUID(), scopeKey)
		val state = requireNotNull(findByScopeKeyForUpdate(scopeKey))
		if (shortageDate == null) {
			state.shortageDate = null
			return BalanceShortageTransition(state.episode, false)
		}
		if (state.shortageDate == shortageDate) {
			return BalanceShortageTransition(state.episode, false)
		}

		state.shortageDate = shortageDate
		state.episode += 1
		return BalanceShortageTransition(state.episode, true)
	}
}
