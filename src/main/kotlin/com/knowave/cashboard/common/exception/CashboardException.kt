package com.knowave.cashboard.common.exception

import org.springframework.http.HttpStatus

open class CashboardException(
	val errorCode: String,
	override val message: String,
	val status: HttpStatus = HttpStatus.BAD_REQUEST,
) : RuntimeException(message)

class NotFoundException(resourceName: String, id: Any) : CashboardException(
	errorCode = "NOT_FOUND",
	message = "$resourceName not found. id=$id",
	status = HttpStatus.NOT_FOUND,
)

class InvalidEnumValueException(enumName: String, value: String) : CashboardException(
	errorCode = "INVALID_ENUM_VALUE",
	message = "Invalid $enumName value: $value",
	status = HttpStatus.BAD_REQUEST,
)
