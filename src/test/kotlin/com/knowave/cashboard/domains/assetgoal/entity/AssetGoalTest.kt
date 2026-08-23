package com.knowave.cashboard.domains.assetgoal.entity

import com.knowave.cashboard.domains.assetgoal.service.dto.UpdateAssetGoalCommand
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class AssetGoalTest {
	@Test
	fun `applyUpdate는 변경된 값만 Entity에 반영한다`() {
		val assetGoal = AssetGoal(
			name = "1억 만들기",
			targetAmount = 100_000_000L,
			targetDate = LocalDate.of(2030, 12, 31),
		)

		AssetGoal.applyUpdate(
			assetGoal = assetGoal,
			command = UpdateAssetGoalCommand(
				name = "2억 만들기",
				targetAmount = 200_000_000L,
				targetDate = LocalDate.of(2032, 12, 31),
			),
		)

		assertThat(assetGoal.name).isEqualTo("2억 만들기")
		assertThat(assetGoal.targetAmount).isEqualTo(200_000_000L)
		assertThat(assetGoal.targetDate).isEqualTo(LocalDate.of(2032, 12, 31))
	}
}
