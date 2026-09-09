package com.knowave.cashboard.domains.assetgoal.repository

import com.knowave.cashboard.domains.assetgoal.entity.AssetGoal
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AssetGoalJpaRepository : JpaRepository<AssetGoal, UUID> {
	fun findByIdAndUserId(id: UUID, userId: UUID): AssetGoal?
	fun findAllByUserId(userId: UUID): List<AssetGoal>
}
