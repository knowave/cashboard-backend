package com.knowave.cashboard.domains.assetgoal.service.dto

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class AssetGoalSummaryResult(
	val id: UUID,
	val name: String,
	val targetAmount: Long,
	val targetDate: LocalDate,
	val currentAssetAmount: Long,
	val remainingAmount: Long,
	val achievementRate: Double,
	val createdAt: LocalDateTime,
	val updatedAt: LocalDateTime,
)

data class AssetGoalDetailResult(
	val id: UUID,
	val name: String,
	val targetAmount: Long,
	val targetDate: LocalDate,
	val currentAssetAmount: Long,
	val remainingAmount: Long,
	val achievementRate: Double,
	val savingPeriodMonths: Int,
	val averageMonthlySavingAmount: Long,
	val requiredMonthlySavingAmount: Long,
	val expectedAchievementDate: LocalDate?,
	val targetAchievable: Boolean,
	val createdAt: LocalDateTime,
	val updatedAt: LocalDateTime,
)

data class AssetGoalSimulationResult(
	val monthlySavingAmount: Long,
	val currentAssetAmount: Long,
	val remainingAmount: Long,
	val requiredMonths: Int,
	val expectedAchievementDate: LocalDate?,
	val targetDate: LocalDate,
	val targetAchievable: Boolean,
)
