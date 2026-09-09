package com.knowave.cashboard.common.exception

import org.springframework.http.HttpStatus

class InvalidTimezoneException(timezone: String) : CashboardException(
	errorCode = "INVALID_TIMEZONE",
	message = "timezone is not a valid IANA zone id. timezone=$timezone",
)

class UnsupportedSocialProviderException(provider: String) : CashboardException(
	errorCode = "UNSUPPORTED_SOCIAL_PROVIDER",
	message = "No SocialAuthProvider supports provider=$provider",
	status = HttpStatus.INTERNAL_SERVER_ERROR,
)

class InvalidRefreshTokenException : CashboardException(
	errorCode = "INVALID_REFRESH_TOKEN",
	message = "Refresh token is invalid, expired, or already used.",
	status = HttpStatus.UNAUTHORIZED,
)
