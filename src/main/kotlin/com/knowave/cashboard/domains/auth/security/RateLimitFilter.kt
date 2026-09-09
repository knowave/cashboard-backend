package com.knowave.cashboard.domains.auth.security

import com.knowave.cashboard.common.exception.ErrorResponse
import com.knowave.cashboard.common.response.ApiResponse
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import tools.jackson.databind.ObjectMapper
import java.time.Clock
import java.util.concurrent.ConcurrentHashMap

private val RATE_LIMITED_PATHS = setOf("/auth/social/login", "/auth/refresh")
private const val WINDOW_MILLIS = 60_000L

// /auth/social/login은 로그인=회원가입 겸용 공개 엔드포인트라 남용이 곧 무제한 User 생성이다.
// ponytail: 인메모리 슬라이딩 윈도 카운터, 다중 인스턴스는 인스턴스별로 따로 센다. IP당 항목이
// 앱 수명 내내 누적되므로(재방문 없는 IP는 정리되지 않음) 인스턴스 다중화나 IP 카디널리티가
// 문제가 되면 Redis 공유 카운터 + TTL로 옮긴다.
@Component
class RateLimitFilter(
	@Value("\${auth.rate-limit.per-minute}") private val perMinuteLimit: Int,
	private val clock: Clock,
	private val objectMapper: ObjectMapper,
) : OncePerRequestFilter() {
	private val requestTimestampsByIp = ConcurrentHashMap<String, MutableList<Long>>()

	override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
		if (request.servletPath !in RATE_LIMITED_PATHS || !exceedsLimit(request.remoteAddr)) {
			filterChain.doFilter(request, response)
			return
		}
		response.status = HttpStatus.TOO_MANY_REQUESTS.value()
		response.contentType = MediaType.APPLICATION_JSON_VALUE
		val errorBody = ApiResponse(
			success = false,
			data = ErrorResponse(code = "RATE_LIMIT_EXCEEDED", message = "Too many requests. Try again later."),
		)
		response.writer.write(objectMapper.writeValueAsString(errorBody))
	}

	private fun exceedsLimit(clientIp: String): Boolean {
		val now = clock.millis()
		val windowStart = now - WINDOW_MILLIS
		val requestTimestamps = requestTimestampsByIp.computeIfAbsent(clientIp) { java.util.Collections.synchronizedList(mutableListOf()) }
		synchronized(requestTimestamps) {
			requestTimestamps.removeIf { it < windowStart }
			if (requestTimestamps.size >= perMinuteLimit) {
				return true
			}
			requestTimestamps.add(now)
			return false
		}
	}
}
