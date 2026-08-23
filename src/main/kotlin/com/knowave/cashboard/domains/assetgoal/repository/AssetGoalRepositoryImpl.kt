package com.knowave.cashboard.domains.assetgoal.repository

import com.knowave.cashboard.domains.assetgoal.entity.AssetGoal
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class AssetGoalRepositoryImpl(
	private val assetGoalJpaRepository: AssetGoalJpaRepository,
) : AssetGoalRepository {
	override fun save(assetGoal: AssetGoal): AssetGoal = assetGoalJpaRepository.save(assetGoal)

	override fun findById(id: UUID): AssetGoal? = assetGoalJpaRepository.findById(id).orElse(null)

	override fun findAll(): List<AssetGoal> = assetGoalJpaRepository.findAll()

	override fun delete(assetGoal: AssetGoal) {
		assetGoalJpaRepository.delete(assetGoal)
	}
}
