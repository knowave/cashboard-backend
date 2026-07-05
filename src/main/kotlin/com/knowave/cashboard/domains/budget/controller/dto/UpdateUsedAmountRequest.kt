package com.knowave.cashboard.domains.budget.controller.dto

import com.knowave.cashboard.domains.budget.service.dto.UpdateUsedAmountCommand
import jakarta.validation.constraints.PositiveOrZero

data class UpdateUsedAmountRequest(
	@field:PositiveOrZero(message = "usedAmount must be greater than or equal to 0.")
	val usedAmount: Long,
) {
	fun toCommand(): UpdateUsedAmountCommand = UpdateUsedAmountCommand(
		usedAmount = usedAmount,
	)
}
