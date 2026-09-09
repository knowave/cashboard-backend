package com.knowave.cashboard.domains.account.repository

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class AccountBalanceLockRepositoryImpl(
	private val jdbcTemplate: JdbcTemplate,
) : AccountBalanceLockRepository {
	override fun acquireTotalAssetLock(userId: UUID) {
		// ponytail: pg_advisory_xact_lock(bigint)는 void를 반환하는 SELECT라 ResultSet이 따라온다.
		// JdbcTemplate.update()는 executeUpdate()를 호출하는데 pgjdbc는 SELECT류에 그걸 허용하지 않는다
		// ("A result was returned when none was expected"). queryForList로 바꿔 결과를 버린다.
		jdbcTemplate.queryForList(
			"SELECT pg_advisory_xact_lock(hashtextextended(?, $TOTAL_ASSET_ADVISORY_LOCK_KEY))",
			userId.toString(),
		)
	}

	private companion object {
		// 사용자별 총자산과 목표 알림 marker 계산 범위를 직렬화하는 잠금 키 seed다.
		// hashtextextended(userId, seed)로 사용자마다 별도의 advisory lock 공간을 만든다 (D10).
		const val TOTAL_ASSET_ADVISORY_LOCK_KEY = 6_103_358_362_453_189_271L
	}
}
