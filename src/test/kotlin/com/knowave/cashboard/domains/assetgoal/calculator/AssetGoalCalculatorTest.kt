package com.knowave.cashboard.domains.assetgoal.calculator

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class AssetGoalCalculatorTest {
	private val calculator = AssetGoalCalculator()

	@Test
	fun `현재 자산과 저축 실적으로 목표 분석값을 계산한다`() {
		val result = calculator.calculate(
			targetAmount = 10_000_000L,
			currentAssetAmount = 4_000_000L,
			targetDate = LocalDate.of(2027, 2, 1),
			savingAmounts = listOf(1_000_000L, 800_000L, 700_000L),
			baseDate = LocalDate.of(2026, 8, 23),
		)

		assertThat(result.remainingAmount).isEqualTo(6_000_000L)
		assertThat(result.achievementRate).isEqualTo(40.0)
		assertThat(result.averageMonthlySavingAmount).isEqualTo(833_333L)
		assertThat(result.requiredMonthlySavingAmount).isEqualTo(1_000_000L)
		assertThat(result.expectedAchievementDate).isEqualTo(LocalDate.of(2027, 4, 23))
		assertThat(result.targetAchievable).isFalse()
	}

	@Test
	fun `평균 저축액이 없으면 예상 달성일은 null이다`() {
		val result = calculator.calculate(
			targetAmount = 10_000_000L,
			currentAssetAmount = 4_000_000L,
			targetDate = LocalDate.of(2027, 2, 1),
			savingAmounts = emptyList(),
			baseDate = LocalDate.of(2026, 8, 23),
		)

		assertThat(result.averageMonthlySavingAmount).isEqualTo(0L)
		assertThat(result.expectedAchievementDate).isNull()
		assertThat(result.targetAchievable).isFalse()
	}

	@Test
	fun `달성 가능 여부는 예상 달성일이 목표일보다 늦지 않은지로 판단한다`() {
		val result = calculator.calculate(
			targetAmount = 10_000_000L,
			currentAssetAmount = 4_000_000L,
			targetDate = LocalDate.of(2027, 2, 1),
			savingAmounts = listOf(1_000_000L),
			baseDate = LocalDate.of(2026, 8, 23),
		)

		assertThat(result.expectedAchievementDate).isEqualTo(LocalDate.of(2027, 2, 23))
		assertThat(result.averageMonthlySavingAmount).isEqualTo(result.requiredMonthlySavingAmount)
		assertThat(result.targetAchievable).isFalse()
	}
}
