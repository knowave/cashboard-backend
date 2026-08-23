package com.knowave.cashboard.domains.assetgoal.controller.dto

import com.knowave.cashboard.domains.assetgoal.service.dto.AssetGoalDetailResult
import com.knowave.cashboard.domains.assetgoal.service.dto.AssetGoalSummaryResult
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class AssetGoalSummaryResponse(
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

data class AssetGoalDetailResponse(
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

fun AssetGoalSummaryResult.toResponse(): AssetGoalSummaryResponse = AssetGoalSummaryResponse(
	id = id,
	name = name,
	targetAmount = targetAmount,
	targetDate = targetDate,
	currentAssetAmount = currentAssetAmount,
	remainingAmount = remainingAmount,
	achievementRate = achievementRate,
	createdAt = createdAt,
	updatedAt = updatedAt,
)

fun AssetGoalDetailResult.toResponse(): AssetGoalDetailResponse = AssetGoalDetailResponse(
	id = id,
	name = name,
	targetAmount = targetAmount,
	targetDate = targetDate,
	currentAssetAmount = currentAssetAmount,
	remainingAmount = remainingAmount,
	achievementRate = achievementRate,
	savingPeriodMonths = savingPeriodMonths,
	averageMonthlySavingAmount = averageMonthlySavingAmount,
	requiredMonthlySavingAmount = requiredMonthlySavingAmount,
	expectedAchievementDate = expectedAchievementDate,
	targetAchievable = targetAchievable,
	createdAt = createdAt,
	updatedAt = updatedAt,
)
