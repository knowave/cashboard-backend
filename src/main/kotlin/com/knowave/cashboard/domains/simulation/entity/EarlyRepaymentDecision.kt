package com.knowave.cashboard.domains.simulation.entity

enum class EarlyRepaymentDecision(
	val description: String,
) {
	PROHIBITED("상환 금지"),
	HOLD("보류"),
	SMALL_AVAILABLE("소액 가능"),
	PARTIAL_AVAILABLE("일부 상환 가능"),
	;

	companion object {
		fun fromAvailableAmount(amount: Long): EarlyRepaymentDecision = when {
			amount < 5_000_000L -> PROHIBITED
			amount < 7_000_000L -> HOLD
			amount < 10_000_000L -> SMALL_AVAILABLE
			else -> PARTIAL_AVAILABLE
		}
	}
}
