package com.knowave.cashboard.domains.notification.listener

import com.knowave.cashboard.common.entity.BaseEntity
import com.knowave.cashboard.domains.assetgoal.entity.AssetGoal
import com.knowave.cashboard.domains.assetgoal.event.AssetGoalChangedEvent
import com.knowave.cashboard.domains.assetgoal.event.TotalAssetAmountChangedEvent
import com.knowave.cashboard.domains.assetgoal.repository.AssetGoalRepository
import com.knowave.cashboard.domains.notification.policy.AssetGoalNotificationPolicy
import com.knowave.cashboard.domains.notification.policy.ThresholdNotificationDecision
import com.knowave.cashboard.domains.notification.service.ThresholdNotificationService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class AssetGoalNotificationEventListenerTest {
	private val goals = FakeAssetGoalRepository()
	private val thresholdService = RecordingThresholdNotificationService()
	private val occurredAt = Instant.parse("2026-09-02T00:00:00Z")
	private val listener = AssetGoalNotificationEventListener(goals, AssetGoalNotificationPolicy(), thresholdService)

	@Test
	fun `총자산 변경은 모든 목표의 마일스톤을 각각 처리한다`() {
		val firstGoalId = UUID.fromString("00000000-0000-0000-0000-000000000001")
		val secondGoalId = UUID.fromString("00000000-0000-0000-0000-000000000002")
		goals.goals = listOf(
			AssetGoal("첫 목표", 100L, LocalDate.now().plusMonths(1)).also { it.assignBaseFields(firstGoalId) },
			AssetGoal("둘째 목표", 200L, LocalDate.now().plusMonths(1)).also { it.assignBaseFields(secondGoalId) },
		)

		listener.on(TotalAssetAmountChangedEvent(40L, 105L, occurredAt))

		assertThat(thresholdService.decisions.map { it.crossedPolicyKeys }).containsExactly(
			listOf("ASSET_GOAL:$firstGoalId:50", "ASSET_GOAL:$firstGoalId:80", "ASSET_GOAL:$firstGoalId:100"),
			listOf("ASSET_GOAL:$secondGoalId:50"),
		)
		assertThat(thresholdService.occurredAts).containsOnly(occurredAt)
	}

	@Test
	fun `목표 변경은 해당 목표의 변경 전후 금액으로 처리한다`() {
		val goalId = UUID.fromString("00000000-0000-0000-0000-000000000001")

		listener.on(AssetGoalChangedEvent(goalId, "내 집", 200L, 100L, 80L, 80L, occurredAt))

		assertThat(thresholdService.decisions.single().crossedPolicyKeys).containsExactly(
			"ASSET_GOAL:$goalId:50",
			"ASSET_GOAL:$goalId:80",
		)
		assertThat(thresholdService.decisions.single().notification?.type?.name).isEqualTo("ASSET_GOAL_PROGRESS")
	}

	@Test
	fun `목표 생성은 0에서 현재 자산 달성률까지 통과한 모든 marker와 최고 단계 후보를 처리한다`() {
		val goalId = UUID.fromString("00000000-0000-0000-0000-000000000001")

		listener.on(AssetGoalChangedEvent(goalId, "내 집", 100L, 100L, 0L, 105L, occurredAt))

		assertThat(thresholdService.decisions.single().crossedPolicyKeys).containsExactly(
			"ASSET_GOAL:$goalId:50",
			"ASSET_GOAL:$goalId:80",
			"ASSET_GOAL:$goalId:100",
		)
		assertThat(thresholdService.decisions.single().notification?.type?.name).isEqualTo("ASSET_GOAL_ACHIEVED")
	}
}

private class FakeAssetGoalRepository : AssetGoalRepository {
	var goals: List<AssetGoal> = emptyList()
	override fun save(assetGoal: AssetGoal): AssetGoal = assetGoal
	override fun findById(id: UUID): AssetGoal? = goals.firstOrNull { it.id == id }
	override fun findAll(): List<AssetGoal> = goals
	override fun delete(assetGoal: AssetGoal) = Unit
}

private class RecordingThresholdNotificationService : ThresholdNotificationService {
	val decisions = mutableListOf<ThresholdNotificationDecision>()
	val occurredAts = mutableListOf<Instant>()
	override fun process(decision: ThresholdNotificationDecision, occurredAt: Instant): Boolean {
		decisions += decision
		occurredAts += occurredAt
		return true
	}
}

private fun BaseEntity.assignBaseFields(id: UUID) {
	val baseClass = BaseEntity::class.java
	baseClass.getDeclaredField("id").apply { isAccessible = true; set(this@assignBaseFields, id) }
	baseClass.getDeclaredField("createdAt").apply { isAccessible = true; set(this@assignBaseFields, LocalDateTime.now()) }
	baseClass.getDeclaredField("updatedAt").apply { isAccessible = true; set(this@assignBaseFields, LocalDateTime.now()) }
}
