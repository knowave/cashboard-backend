package com.knowave.cashboard.domains.assetgoal.controller.dto

import com.knowave.cashboard.domains.assetgoal.service.dto.UpdateSavingRecordCommand
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size

data class SavingRecordUpdateRequest(
	@field:Pattern(regexp = "\\d{4}-\\d{2}", message = "targetMonth must be yyyy-MM.")
	val targetMonth: String,

	@field:PositiveOrZero(message = "amount must be greater than or equal to 0.")
	val amount: Long,

	@field:Size(max = 255, message = "memo must be less than or equal to 255 characters.")
	val memo: String?,
) {
	fun toCommand(): UpdateSavingRecordCommand = UpdateSavingRecordCommand(
		targetMonth = targetMonth,
		amount = amount,
		memo = memo,
	)
}
