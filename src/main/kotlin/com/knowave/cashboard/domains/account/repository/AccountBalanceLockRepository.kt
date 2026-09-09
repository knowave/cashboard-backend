package com.knowave.cashboard.domains.account.repository

import java.util.UUID

interface AccountBalanceLockRepository {
	fun acquireTotalAssetLock(userId: UUID)
}
