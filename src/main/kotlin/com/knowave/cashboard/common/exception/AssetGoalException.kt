package com.knowave.cashboard.common.exception

import org.springframework.http.HttpStatus
import java.util.UUID

class AssetGoalNotFoundException(id: UUID) : CashboardException(
	errorCode = "ASSET_GOAL_NOT_FOUND",
	message = "AssetGoal not found. id=$id",
	status = HttpStatus.NOT_FOUND,
)

class SavingRecordNotFoundException(value: Any) : CashboardException(
	errorCode = "SAVING_RECORD_NOT_FOUND",
	message = "SavingRecord not found. value=$value",
	status = HttpStatus.NOT_FOUND,
)

class DuplicateSavingRecordException(targetMonth: String) : CashboardException(
	errorCode = "DUPLICATE_SAVING_RECORD",
	message = "SavingRecord already exists. targetMonth=$targetMonth",
	status = HttpStatus.CONFLICT,
)

class InvalidSavingPeriodException(periodMonths: Int) : CashboardException(
	errorCode = "INVALID_SAVING_PERIOD",
	message = "savingPeriodMonths must be one of 3, 6, 12, 24, 36. savingPeriodMonths=$periodMonths",
	status = HttpStatus.BAD_REQUEST,
)

class InvalidAssetGoalException(reason: String) : CashboardException(
	errorCode = "INVALID_ASSET_GOAL",
	message = reason,
	status = HttpStatus.BAD_REQUEST,
)
