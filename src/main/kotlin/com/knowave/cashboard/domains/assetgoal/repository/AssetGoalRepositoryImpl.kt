package com.knowave.cashboard.domains.assetgoal.repository

import com.knowave.cashboard.domains.assetgoal.entity.AssetGoal
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class AssetGoalRepositoryImpl(
	private val assetGoalJpaRepository: AssetGoalJpaRepository,
) : AssetGoalRepository {
	override fun save(assetGoal: AssetGoal): AssetGoal = assetGoalJpaRepository.save(assetGoal)

	override fun findByIdAndUserId(id: UUID, userId: UUID): AssetGoal? =
		assetGoalJpaRepository.findByIdAndUserId(id, userId)

	override fun findAllByUserId(userId: UUID): List<AssetGoal> = assetGoalJpaRepository.findAllByUserId(userId)

	override fun delete(assetGoal: AssetGoal) {
		assetGoalJpaRepository.delete(assetGoal)
	}
}
