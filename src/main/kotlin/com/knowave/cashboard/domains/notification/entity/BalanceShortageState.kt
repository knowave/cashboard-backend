package com.knowave.cashboard.domains.notification.entity

import com.knowave.cashboard.common.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "balance_shortage_states")
class BalanceShortageState(
	@Column(name = "scope_key", nullable = false, unique = true, length = 50)
	val scopeKey: String = "SINGLE_USER",
	@Column(name = "shortage_date")
	var shortageDate: LocalDate? = null,
	@Column(nullable = false)
	var episode: Long = 0,
) : BaseEntity()
