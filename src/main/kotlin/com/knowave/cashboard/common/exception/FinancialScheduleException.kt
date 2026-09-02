package com.knowave.cashboard.common.exception

import org.springframework.http.HttpStatus
import java.util.UUID

class InvalidFinancialScheduleException(reason: String) : CashboardException(
	errorCode = "VALIDATION_ERROR",
	message = reason,
)

class InvalidRecurrenceRuleException(reason: String) : CashboardException(
	errorCode = "INVALID_RECURRENCE_RULE",
	message = reason,
)

class InvalidFinancialSchedulePeriodException(reason: String) : CashboardException(
	errorCode = "INVALID_PERIOD",
	message = reason,
)

class EmptyFinancialSchedulePatchException : CashboardException(
	errorCode = "EMPTY_PATCH",
	message = "At least one field must be provided.",
)

class ProjectionRangeExceededException(months: Long) : CashboardException(
	errorCode = "PROJECTION_RANGE_EXCEEDED",
	message = "Projection range must be less than or equal to 600 months. months=$months",
)

class FinancialScheduleDataIntegrityException(
	id: UUID,
	field: String,
	cause: Throwable,
) : CashboardException(
	errorCode = "DATA_INTEGRITY_ERROR",
	message = "FinancialSchedule contains an invalid persisted value. id=$id, field=$field",
	status = HttpStatus.INTERNAL_SERVER_ERROR,
) {
	init {
		initCause(cause)
	}
}
