package com.knowave.cashboard.domains.assetgoal.entity

import com.knowave.cashboard.common.exception.InvalidSavingPeriodException

enum class SavingPeriod(val months: Int) {
	THREE(3),
	SIX(6),
	TWELVE(12),
	TWENTY_FOUR(24),
	THIRTY_SIX(36),
	;

	companion object {
		fun from(months: Int): SavingPeriod = entries.firstOrNull { it.months == months }
			?: throw InvalidSavingPeriodException(months)
	}
}
