package com.knowave.cashboard.common.exception

import org.springframework.http.HttpStatus

class NicknameGenerationFailedException(providerNickname: String?) : CashboardException(
	errorCode = "NICKNAME_GENERATION_FAILED",
	message = "Failed to generate a unique nickname after retries. providerNickname=$providerNickname",
	status = HttpStatus.INTERNAL_SERVER_ERROR,
)

/**
 * insertIfAbsent가 성공(또는 ON CONFLICT DO NOTHING)을 보고했는데 재조회에서 행이 없는 경우.
 * (provider, provider_id) UNIQUE와 재조회 조건이 어긋난 것이므로 클라이언트 잘못이 아니다.
 */
class UserNotPersistedException(providerName: String, providerId: String) : CashboardException(
	errorCode = "USER_NOT_PERSISTED",
	message = "User insert reported success but re-query found none. providerName=$providerName, providerId=$providerId",
	status = HttpStatus.INTERNAL_SERVER_ERROR,
)
