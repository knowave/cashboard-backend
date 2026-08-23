package com.knowave.cashboard.domains.assetgoal.entity

import com.knowave.cashboard.common.entity.BaseEntity
import com.knowave.cashboard.domains.assetgoal.service.dto.UpdateAssetGoalCommand
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "asset_goals")
class AssetGoal(
	@Column(name = "name", nullable = false, length = 100)
	var name: String,

	@Column(name = "target_amount", nullable = false)
	var targetAmount: Long,

	@Column(name = "target_date", nullable = false)
	var targetDate: LocalDate,
) : BaseEntity() {
	companion object {
		fun applyUpdate(assetGoal: AssetGoal, command: UpdateAssetGoalCommand): AssetGoal {
			if (assetGoal.name != command.name) {
				assetGoal.name = command.name
			}
			if (assetGoal.targetAmount != command.targetAmount) {
				assetGoal.targetAmount = command.targetAmount
			}
			if (assetGoal.targetDate != command.targetDate) {
				assetGoal.targetDate = command.targetDate
			}
			return assetGoal
		}
	}
}
