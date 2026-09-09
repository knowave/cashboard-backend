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
	fun findByUserIdForUpdate(userId: UUID): BalanceShortageState?
	fun save(state: BalanceShortageState): BalanceShortageState
	fun transition(userId: UUID, shortageDate: LocalDate?): BalanceShortageTransition
}

interface BalanceShortageStateJpaRepository : JpaRepository<BalanceShortageState, UUID> {
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	fun findByUserId(userId: UUID): BalanceShortageState?

	// ponytail: scope_key is still NOT NULL (V8 hasn't dropped it) so it stays hardcoded here.
	// ON CONFLICT targets user_id in anticipation of V8's UNIQUE(user_id); until V8 runs, a
	// second user's insert can raise a duplicate-key error against the still-global
	// UNIQUE(scope_key) instead of being silently skipped. Expected pre-V8 breakage.
	@Modifying
	@Query(
		value = """
			INSERT INTO balance_shortage_states(id, user_id, scope_key, shortage_date, episode, created_at, updated_at)
			VALUES (:id, :userId, 'SINGLE_USER', NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
			ON CONFLICT (user_id) DO NOTHING
		""",
		nativeQuery = true,
	)
	fun insertInitialIfAbsent(@Param("id") id: UUID, @Param("userId") userId: UUID): Int
}

@Repository
class BalanceShortageStateRepositoryImpl(
	private val balanceShortageStateJpaRepository: BalanceShortageStateJpaRepository,
) : BalanceShortageStateRepository {
	override fun findByUserIdForUpdate(userId: UUID): BalanceShortageState? =
		balanceShortageStateJpaRepository.findByUserId(userId)

	override fun save(state: BalanceShortageState): BalanceShortageState = balanceShortageStateJpaRepository.save(state)

	@Transactional
	override fun transition(userId: UUID, shortageDate: LocalDate?): BalanceShortageTransition {
		balanceShortageStateJpaRepository.insertInitialIfAbsent(UUID.randomUUID(), userId)
		val state = requireNotNull(findByUserIdForUpdate(userId))
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
