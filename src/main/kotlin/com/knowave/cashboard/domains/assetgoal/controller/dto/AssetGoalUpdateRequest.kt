package com.knowave.cashboard.domains.assetgoal.controller.dto

import com.knowave.cashboard.domains.assetgoal.service.dto.UpdateAssetGoalCommand
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.time.LocalDate

data class AssetGoalUpdateRequest(
	@field:NotBlank(message = "name must not be blank.")
	@field:Size(max = 100, message = "name must be less than or equal to 100 characters.")
	val name: String,

	@field:Positive(message = "targetAmount must be greater than 0.")
	val targetAmount: Long,

	@field:Future(message = "targetDate must be a future date.")
	val targetDate: LocalDate,
) {
	fun toCommand(): UpdateAssetGoalCommand = UpdateAssetGoalCommand(
		name = name,
		targetAmount = targetAmount,
		targetDate = targetDate,
	)
}
