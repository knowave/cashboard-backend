package com.knowave.cashboard.domains.assetgoal.repository

import com.knowave.cashboard.domains.assetgoal.entity.AssetGoal
import java.util.UUID

interface AssetGoalRepository {
	fun save(assetGoal: AssetGoal): AssetGoal
	fun findById(id: UUID): AssetGoal?
	fun findAll(): List<AssetGoal>
	fun delete(assetGoal: AssetGoal)
}
