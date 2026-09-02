package com.knowave.cashboard.domains.financialschedule.context

interface LiquidityBalanceProvider {
	fun getCurrentLiquidBalance(): Long
}
