package com.knowave.cashboard.domains.user.service

import java.time.Instant
import java.util.UUID

/**
 * Refresh Token 저장·회전·폐기. 원문을 받아 내부에서 해싱하므로 호출자는 해시 방식을 모른다.
 *
 * 만료 정책(TTL)은 호출자가 정한다 — 이 서비스는 주어진 expiresAt을 그대로 저장한다.
 */
interface RefreshTokenService {
	fun issue(userId: UUID, refreshToken: String, expiresAt: Instant)

	/**
	 * 원자적으로 활성 토큰을 폐기하고 소유 userId를 반환한다. 실패하면 null이다.
	 *
	 * 이미 폐기·만료된 토큰의 재사용이면(= 탈취 의심) 그 User의 전 세션을 폐기하고 null을 반환한다
	 * (OAuth 2.0 Security BCP). 호출자는 실패 사유를 구분할 필요 없이 null만 보고 거절하면 된다.
	 */
	fun rotate(refreshToken: String, now: Instant): UUID?

	fun revoke(refreshToken: String, now: Instant, logoutAllDevices: Boolean)
}
