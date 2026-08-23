package com.knowave.cashboard.domains.assetgoal.controller.dto

import com.knowave.cashboard.domains.assetgoal.service.dto.AssetGoalSimulationResult
import java.time.LocalDate

data class AssetGoalSimulationResponse(
	val monthlySavingAmount: Long,
	val currentAssetAmount: Long,
	val remainingAmount: Long,
	val requiredMonths: Int,
	val expectedAchievementDate: LocalDate?,
	val targetDate: LocalDate,
	val targetAchievable: Boolean,
)

fun AssetGoalSimulationResult.toResponse(): AssetGoalSimulationResponse = AssetGoalSimulationResponse(
	monthlySavingAmount = monthlySavingAmount,
	currentAssetAmount = currentAssetAmount,
	remainingAmount = remainingAmount,
	requiredMonths = requiredMonths,
	expectedAchievementDate = expectedAchievementDate,
	targetDate = targetDate,
	targetAchievable = targetAchievable,
)
