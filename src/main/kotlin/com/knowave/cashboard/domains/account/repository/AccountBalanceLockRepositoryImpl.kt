package com.knowave.cashboard.domains.account.repository

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class AccountBalanceLockRepositoryImpl(
	private val jdbcTemplate: JdbcTemplate,
) : AccountBalanceLockRepository {
	override fun acquireTotalAssetLock() {
		jdbcTemplate.execute("SELECT pg_advisory_xact_lock($TOTAL_ASSET_ADVISORY_LOCK_KEY)")
	}

	private companion object {
		// 모든 계좌의 총자산과 목표 알림 marker 계산 범위를 직렬화하는 전역 잠금 키다.
		const val TOTAL_ASSET_ADVISORY_LOCK_KEY = 6_103_358_362_453_189_271L
	}
}
