package com.knowave.cashboard.domains.auth.social

import com.knowave.cashboard.domains.auth.exception.SocialAuthProviderUnavailableException
import com.knowave.cashboard.domains.auth.exception.SocialAuthenticationFailedException
import org.slf4j.Logger
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestClientResponseException

/**
 * Google/Kakao/Naver/Apple 4종이 공유하는 "code -> token" 교환 형태 + 실패 매핑.
 * provider별로 다른 부분(URI, client_secret 생성 방식, userinfo 매핑)만 각 구현체에 남긴다.
 *
 * ponytail: 응답 본문은 어떤 로그·예외에도 담지 않는다. 이 요청 body에는 매 로그인마다
 * client_secret(3종) 또는 서명된 JWT(Apple)가 실리므로, 실패 응답 본문에도 Provider의
 * 민감한 에러 상세가 섞여 나올 수 있다. status + provider만 남긴다.
 */
internal object ProviderHttpSupport {
	fun exchangeToken(
		restClient: RestClient,
		logger: Logger,
		provider: SocialProvider,
		tokenUri: String,
		clientId: String,
		clientSecret: String,
		authorizationCode: String,
		pkceCodeVerifier: String,
		redirectUri: String,
	): ProviderTokenResponse = callProvider(logger, provider) {
		restClient.post()
			.uri(tokenUri)
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.body(
				LinkedMultiValueMap<String, String>().apply {
					add("grant_type", "authorization_code")
					add("client_id", clientId)
					add("client_secret", clientSecret)
					add("code", authorizationCode)
					add("code_verifier", pkceCodeVerifier)
					add("redirect_uri", redirectUri)
				},
			)
			.retrieve()
			.body(ProviderTokenResponse::class.java)
	}

	// 401(재로그인 필요) vs 502(재시도 가능)의 구분선은 "클라이언트가 재로그인해야 하는가"다.
	// Provider가 4xx면 code/client 자체가 틀린 것 — 401. 5xx·타임아웃·네트워크 실패·파싱 실패는
	// 우리도 클라이언트도 아닌 Provider 쪽 문제 — 502. 재로그인 유도는 이 경우 오진이다.
	fun <T> callProvider(logger: Logger, provider: SocialProvider, providerCall: () -> T?): T {
		try {
			// ponytail: 여기만 의도적으로 표준 예외를 던진다 — CashboardException 하위로 바꾸지 말 것.
			// requireNotNull이 던지는 IllegalArgumentException을 아래 catch가 잡아
			// SocialAuthProviderUnavailableException(502)으로 변환하는 것이 이 함수의 설계다.
			// 예외 클래스로 바꾸면 그 catch가 놓쳐 502 매핑이 사라진다.
			return requireNotNull(providerCall()) { "Empty response from social login provider. provider=$provider" }
		} catch (exception: RestClientResponseException) {
			if (exception.statusCode.is4xxClientError) {
				logger.debug("Social login provider rejected the request. provider={}, status={}", provider, exception.statusCode.value())
				throw SocialAuthenticationFailedException(provider)
			}
			logger.warn("Social login provider call failed. provider={}, status={}", provider, exception.statusCode.value())
			throw SocialAuthProviderUnavailableException(provider)
		} catch (exception: RestClientException) {
			logger.warn("Social login provider call failed. provider={}, reason={}", provider, exception.javaClass.simpleName)
			throw SocialAuthProviderUnavailableException(provider)
		} catch (exception: IllegalArgumentException) {
			logger.warn("Social login provider returned an unexpected response. provider={}", provider)
			throw SocialAuthProviderUnavailableException(provider)
		}
	}
}
