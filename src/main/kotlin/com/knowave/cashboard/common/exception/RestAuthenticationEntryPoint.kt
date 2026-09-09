package com.knowave.cashboard.common.exception

import com.knowave.cashboard.common.response.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

// GlobalExceptionHandler는 DispatcherServlet 예외만 잡으므로, Security 필터 단계에서
// 끝나는 인증 실패는 여기서 같은 ErrorResponse 형태로 응답한다.
@Component
class RestAuthenticationEntryPoint(
	private val objectMapper: ObjectMapper,
) : AuthenticationEntryPoint {
	override fun commence(
		request: HttpServletRequest,
		response: HttpServletResponse,
		authException: AuthenticationException,
	) {
		response.status = HttpStatus.UNAUTHORIZED.value()
		response.contentType = MediaType.APPLICATION_JSON_VALUE
		val body = ApiResponse(
			success = false,
			data = ErrorResponse(code = "UNAUTHORIZED", message = "Authentication is required."),
		)
		response.writer.write(objectMapper.writeValueAsString(body))
	}
}
