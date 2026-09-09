package com.knowave.cashboard.common.exception

import com.knowave.cashboard.common.response.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

@Component
class RestAccessDeniedHandler(
	private val objectMapper: ObjectMapper,
) : AccessDeniedHandler {
	override fun handle(
		request: HttpServletRequest,
		response: HttpServletResponse,
		accessDeniedException: AccessDeniedException,
	) {
		response.status = HttpStatus.FORBIDDEN.value()
		response.contentType = MediaType.APPLICATION_JSON_VALUE
		val body = ApiResponse(
			success = false,
			data = ErrorResponse(code = "FORBIDDEN", message = "Access is denied."),
		)
		response.writer.write(objectMapper.writeValueAsString(body))
	}
}
