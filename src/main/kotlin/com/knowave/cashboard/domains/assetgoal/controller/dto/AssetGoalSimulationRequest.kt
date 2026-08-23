package com.knowave.cashboard.domains.assetgoal.controller.dto

import com.knowave.cashboard.domains.assetgoal.service.dto.AssetGoalSimulationCommand
import jakarta.validation.constraints.Positive

data class AssetGoalSimulationRequest(
	@field:Positive(message = "monthlySavingAmount must be greater than 0.")
	val monthlySavingAmount: Long,
) {
	fun toCommand(): AssetGoalSimulationCommand = AssetGoalSimulationCommand(
		monthlySavingAmount = monthlySavingAmount,
	)
}
