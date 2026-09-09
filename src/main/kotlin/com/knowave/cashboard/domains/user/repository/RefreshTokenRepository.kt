package com.knowave.cashboard.domains.user.repository

import java.time.Instant
import java.util.UUID

interface RefreshTokenRepository {
	fun save(userId: UUID, tokenHash: String, expiresAt: Instant)

	/**
	 * 원자적 단일 UPDATE로 활성 토큰을 폐기한다. 조회 후 갱신하는 2단 구조는 동일 Refresh로
	 * 온 동시 요청이 둘 다 성공해 세션이 두 갈래로 갈라지는 것을 허용하므로 쓰지 않는다.
	 * 영향 행이 0건이면(이미 폐기됐거나, 만료됐거나, 존재하지 않으면) null을 반환한다.
	 */
	fun revokeIfActive(tokenHash: String, now: Instant): UUID?

	fun deleteAllByUserId(userId: UUID)
	fun existsByTokenHash(tokenHash: String): Boolean

	/**
	 * ponytail: 계약에 없던 추가 메서드. revokeIfActive가 null을 반환했을 때(이미 폐기된 토큰의
	 * 재사용 시도) 어느 User의 세션을 전부 끊어야 하는지 알아내려면 tokenHash로 userId를
	 * 조회할 방법이 필요하다. existsByTokenHash만으로는 userId를 얻을 수 없다.
	 */
	fun findUserIdByTokenHash(tokenHash: String): UUID?
}
