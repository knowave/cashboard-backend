package com.knowave.cashboard.common.exception

data class ErrorResponse(
	val code: String,
	val message: String,
	val errors: List<FieldErrorResponse> = emptyList(),
)

data class FieldErrorResponse(
	val field: String,
	val message: String,
)
