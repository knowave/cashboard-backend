package com.knowave.cashboard.domains.auth.security

import java.util.UUID

/**
 * Cashboard Access Token 발급 전용. 검증은 common/config/SecurityConfig의 NimbusJwtDecoder가
 * 전역 필터 체인에서 수행한다 — 검증 규칙이 두 곳으로 갈라지지 않도록 여기에는 두지 않는다.
 */
interface JwtTokenProvider {
	fun issueAccessToken(userId: UUID): String
}
