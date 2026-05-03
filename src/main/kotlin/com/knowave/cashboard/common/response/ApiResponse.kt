package com.knowave.cashboard.common.response

data class ApiResponse<T>(
	val success: Boolean,
	val data: T? = null,
)

fun <T> success(data: T): ApiResponse<T> = ApiResponse(success = true, data = data)
