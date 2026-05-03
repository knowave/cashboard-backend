package com.knowave.cashboard.common.exception

import com.knowave.cashboard.common.response.ApiResponse
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.format.DateTimeParseException

@RestControllerAdvice
class GlobalExceptionHandler {
	@ExceptionHandler(CashboardException::class)
	fun handleCashboardException(exception: CashboardException): ResponseEntity<ApiResponse<ErrorResponse>> {
		val response = ApiResponse(
			success = false,
			data = ErrorResponse(
				code = exception.errorCode,
				message = exception.message,
			),
		)
		return ResponseEntity.status(exception.status).body(response)
	}

	@ExceptionHandler(MethodArgumentNotValidException::class)
	fun handleValidationException(exception: MethodArgumentNotValidException): ResponseEntity<ApiResponse<ErrorResponse>> {
		val errors = exception.bindingResult.fieldErrors.map {
			FieldErrorResponse(field = it.field, message = it.defaultMessage ?: "Invalid value")
		}
		val response = ApiResponse(
			success = false,
			data = ErrorResponse(
				code = "VALIDATION_ERROR",
				message = "Request validation failed.",
				errors = errors,
			),
		)
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
	}

	@ExceptionHandler(ConstraintViolationException::class)
	fun handleConstraintViolationException(exception: ConstraintViolationException): ResponseEntity<ApiResponse<ErrorResponse>> {
		val errors = exception.constraintViolations.map {
			FieldErrorResponse(field = it.propertyPath.toString(), message = it.message)
		}
		val response = ApiResponse(
			success = false,
			data = ErrorResponse(
				code = "VALIDATION_ERROR",
				message = "Request validation failed.",
				errors = errors,
			),
		)
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
	}

	@ExceptionHandler(DateTimeParseException::class)
	fun handleDateTimeParseException(exception: DateTimeParseException): ResponseEntity<ApiResponse<ErrorResponse>> {
		val response = ApiResponse(
			success = false,
			data = ErrorResponse(
				code = "INVALID_DATE_FORMAT",
				message = "Date value must be valid yyyy-MM.",
			),
		)
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
	}

	@ExceptionHandler(Exception::class)
	fun handleException(exception: Exception): ResponseEntity<ApiResponse<ErrorResponse>> {
		val response = ApiResponse(
			success = false,
			data = ErrorResponse(
				code = "INTERNAL_SERVER_ERROR",
				message = exception.message ?: "Unexpected server error.",
			),
		)
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
	}
}
