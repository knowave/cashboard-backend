package com.knowave.cashboard.domains.assetgoal.repository

import com.knowave.cashboard.domains.assetgoal.entity.AssetGoal
import java.util.UUID

interface AssetGoalRepository {
	fun save(assetGoal: AssetGoal): AssetGoal
	fun findByIdAndUserId(id: UUID, userId: UUID): AssetGoal?
	fun findAllByUserId(userId: UUID): List<AssetGoal>
	fun delete(assetGoal: AssetGoal)
}
