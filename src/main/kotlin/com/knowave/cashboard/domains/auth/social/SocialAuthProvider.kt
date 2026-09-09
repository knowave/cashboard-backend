package com.knowave.cashboard.domains.auth.social

/**
 * Provider별 authorization code -> token 교환 + 사용자 정보 조회를 감춘다.
 *
 * Apple만 client_secret이 ES256 JWT이고 userinfo 엔드포인트 대신 id_token 클레임을 읽는 등
 * 비대칭이 있지만, 그 비대칭은 구현체 안에 가둔다 — 호출자는 4종을 동일하게 다룬다.
 */
interface SocialAuthProvider {
	fun supports(provider: SocialProvider): Boolean
	fun authenticate(authorizationCode: String, pkceCodeVerifier: String, platform: ClientPlatform): SocialUserInfo
}
