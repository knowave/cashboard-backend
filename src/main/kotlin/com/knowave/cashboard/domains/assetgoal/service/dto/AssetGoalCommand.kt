package com.knowave.cashboard.domains.assetgoal.service.dto

import com.knowave.cashboard.domains.assetgoal.entity.AssetGoal
import java.time.LocalDate

data class CreateAssetGoalCommand(
	val name: String,
	val targetAmount: Long,
	val targetDate: LocalDate,
) {
	fun toEntity(): AssetGoal = AssetGoal(
		name = name,
		targetAmount = targetAmount,
		targetDate = targetDate,
	)
}

data class UpdateAssetGoalCommand(
	val name: String,
	val targetAmount: Long,
	val targetDate: LocalDate,
)

data class AssetGoalSimulationCommand(
	val monthlySavingAmount: Long,
)
