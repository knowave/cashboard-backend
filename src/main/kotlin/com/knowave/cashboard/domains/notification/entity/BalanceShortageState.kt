package com.knowave.cashboard.domains.notification.entity

import com.knowave.cashboard.common.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "balance_shortage_states")
class BalanceShortageState(
	@Column(name = "user_id", nullable = false, updatable = false)
	val userId: UUID,
	// ponytail: scope_key stays mapped and hardcoded until Stage 4 drops the column with V8.
	// Removing the mapping now would leave a NOT NULL column with no value on INSERT.
	@Column(name = "scope_key", nullable = false, unique = true, length = 50)
	val scopeKey: String = "SINGLE_USER",
	@Column(name = "shortage_date")
	var shortageDate: LocalDate? = null,
	@Column(nullable = false)
	var episode: Long = 0,
) : BaseEntity()
