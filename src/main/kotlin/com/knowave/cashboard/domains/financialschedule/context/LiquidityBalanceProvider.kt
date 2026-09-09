package com.knowave.cashboard.domains.financialschedule.context

import java.util.UUID

interface LiquidityBalanceProvider {
	fun getCurrentLiquidBalance(userId: UUID): Long
}
