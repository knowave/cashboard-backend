package com.knowave.cashboard.domains.account.entity

import com.knowave.cashboard.common.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "accounts")
class Account(
	@Column(name = "name", nullable = false)
	var name: String,

	@Column(name = "type", nullable = false, length = 50)
	var type: String,

	@Column(name = "balance", nullable = false)
	var balance: Long,
) : BaseEntity() {
	fun update(name: String, type: AccountType, balance: Long) {
		this.name = name
		this.type = type.name
		this.balance = balance
	}
}
