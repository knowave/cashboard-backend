package com.knowave.cashboard.domains.notification.policy

import com.knowave.cashboard.domains.notification.entity.NotificationType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class AssetGoalNotificationPolicyTest {
	private val policy = AssetGoalNotificationPolicy()
	private val userId = UUID.randomUUID()
	private val goalId = UUID.fromString("00000000-0000-0000-0000-000000000001")
	private val occurredAt = Instant.parse("2026-09-02T00:00:00Z")

	@Test
	fun `40에서 105퍼센트가 되면 모든 marker와 달성 알림만 반환한다`() {
		val decision = policy.evaluate(userId, goalId, "1억 만들기", 100L, 100L, 40L, 105L, occurredAt)

		assertThat(decision.crossedPolicyKeys).containsExactly(
			"ASSET_GOAL:$goalId:50",
			"ASSET_GOAL:$goalId:80",
			"ASSET_GOAL:$goalId:100",
		)
		assertThat(decision.selectedPolicyKey).isEqualTo("ASSET_GOAL:$goalId:100")
		assertThat(decision.notification?.type).isEqualTo(NotificationType.ASSET_GOAL_ACHIEVED)
		assertThat(decision.notification?.message).contains("1억 만들기")
	}

	@Test
	fun `50과 80 경계를 각각 통과하면 진행 알림을 반환한다`() {
		val atFifty = policy.evaluate(userId, goalId, "내 집", 100L, 100L, 49L, 50L, occurredAt)
		val atEighty = policy.evaluate(userId, goalId, "내 집", 100L, 100L, 79L, 80L, occurredAt)

		assertThat(atFifty.crossedPolicyKeys).containsExactly("ASSET_GOAL:$goalId:50")
		assertThat(atFifty.notification?.type).isEqualTo(NotificationType.ASSET_GOAL_PROGRESS)
		assertThat(atFifty.notification?.message).contains("50%", "50")
		assertThat(atEighty.crossedPolicyKeys).containsExactly("ASSET_GOAL:$goalId:80")
		assertThat(atEighty.notification?.type).isEqualTo(NotificationType.ASSET_GOAL_PROGRESS)
		assertThat(atEighty.notification?.message).contains("80%", "20")
	}

	@Test
	fun `달성률이 하락하면 알림 후보를 반환하지 않는다`() {
		val decision = policy.evaluate(userId, goalId, "내 집", 100L, 100L, 80L, 40L, occurredAt)

		assertThat(decision.crossedPolicyKeys).isEmpty()
		assertThat(decision.notification).isNull()
	}

	@Test
	fun `0 이하 목표 금액은 알림 후보를 반환하지 않는다`() {
		val decision = policy.evaluate(userId, goalId, "내 집", 0L, 100L, 0L, 100L, occurredAt)

		assertThat(decision.crossedPolicyKeys).isEmpty()
		assertThat(decision.notification).isNull()
	}

	@Test
	fun `큰 금액에서도 교차 곱으로 100퍼센트 경계를 정확히 판별한다`() {
		val target = Long.MAX_VALUE
		val decision = policy.evaluate(userId, goalId, "큰 목표", target, target, target - 1, target, occurredAt)

		assertThat(decision.crossedPolicyKeys).containsExactly("ASSET_GOAL:$goalId:100")
		assertThat(decision.notification?.type).isEqualTo(NotificationType.ASSET_GOAL_ACHIEVED)
	}
}
