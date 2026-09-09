package com.knowave.cashboard.domains.auth.exception

import com.knowave.cashboard.common.exception.CashboardException
import com.knowave.cashboard.domains.auth.social.ClientPlatform
import com.knowave.cashboard.domains.auth.social.SocialProvider
import org.springframework.http.HttpStatus

class UnsupportedSocialLoginException(platform: ClientPlatform, provider: SocialProvider) : CashboardException(
	errorCode = "UNSUPPORTED_SOCIAL_LOGIN",
	message = "Unsupported platform-provider combination. platform=$platform, provider=$provider",
	status = HttpStatus.BAD_REQUEST,
)

class ProviderNotConfiguredException(provider: SocialProvider) : CashboardException(
	errorCode = "PROVIDER_NOT_CONFIGURED",
	message = "Social login provider is not configured. provider=$provider",
	status = HttpStatus.SERVICE_UNAVAILABLE,
)

class SocialAuthenticationFailedException(provider: SocialProvider) : CashboardException(
	errorCode = "SOCIAL_AUTHENTICATION_FAILED",
	message = "Social login authentication failed. provider=$provider",
	status = HttpStatus.UNAUTHORIZED,
)

// 5xx/타임아웃/네트워크 실패/파싱 실패 — 우리 잘못도 클라이언트 잘못도 아니다. 401과 달리
// 재로그인이 아니라 재시도가 맞는 상황이므로 별도 상태 코드로 구분한다.
class SocialAuthProviderUnavailableException(provider: SocialProvider) : CashboardException(
	errorCode = "SOCIAL_AUTH_PROVIDER_UNAVAILABLE",
	message = "Social login provider is unavailable. provider=$provider",
	status = HttpStatus.BAD_GATEWAY,
)
