package com.knowave.cashboard.domains.assetgoal.service.dto

import com.knowave.cashboard.domains.assetgoal.entity.AssetGoal
import java.time.LocalDate
import java.util.UUID

data class CreateAssetGoalCommand(
	val name: String,
	val targetAmount: Long,
	val targetDate: LocalDate,
) {
	fun toEntity(userId: UUID): AssetGoal = AssetGoal(
		userId = userId,
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
