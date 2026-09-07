package com.knowave.cashboard.domains.notification.policy

import com.knowave.cashboard.domains.budget.event.BudgetUsageChangedEvent
import com.knowave.cashboard.domains.notification.entity.NotificationType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.math.BigInteger
import java.time.Instant
import java.util.UUID
import java.util.stream.Stream

class BudgetNotificationPolicyTest {
	private val policy = BudgetNotificationPolicy()
	private val budgetId = UUID.fromString("11111111-1111-1111-1111-111111111111")

	@Test
	fun `70에서 105퍼센트가 되면 두 marker와 초과 알림만 반환한다`() {
		val decision = policy.evaluate(event(previousUsed = 70, currentUsed = 105, budget = 100))

		assertThat(decision.crossedPolicyKeys).containsExactly("BUDGET:$budgetId:80", "BUDGET:$budgetId:100")
		assertThat(decision.selectedPolicyKey).isEqualTo("BUDGET:$budgetId:100")
		assertThat(decision.notification?.type).isEqualTo(NotificationType.BUDGET_EXCEEDED)
		assertThat(decision.notification?.title).isEqualTo("이번 달 예산을 초과했어요")
		assertThat(decision.notification?.message).contains("105%")
	}

	@Test
	fun `하락은 알림 결정을 만들지 않는다`() {
		val decision = policy.evaluate(event(previousUsed = 105, currentUsed = 70, budget = 100))

		assertThat(decision.crossedPolicyKeys).isEmpty()
		assertThat(decision.notification).isNull()
	}

	@ParameterizedTest
	@MethodSource("budgetTransitions")
	fun `예산 경계 전이를 평가한다`(case: BudgetCase) {
		val decision = policy.evaluate(case.event)

		assertThat(decision.notification?.type).isEqualTo(case.expectedType)
		case.expectedCrossedPolicyKeys?.let { expectedKeys ->
			assertThat(decision.crossedPolicyKeys).containsExactlyElementsOf(expectedKeys)
		}
	}

	private fun event(
		previousUsed: Long,
		currentUsed: Long,
		budget: Long,
		previousBudget: Long = budget,
		currentBudget: Long = budget,
	): BudgetUsageChangedEvent = BudgetUsageChangedEvent(
		monthlyBudgetId = budgetId,
		previousBudgetAmount = previousBudget,
		previousUsedAmount = previousUsed,
		currentBudgetAmount = currentBudget,
		currentUsedAmount = currentUsed,
		occurredAt = OCCURRED_AT,
	)

	data class BudgetCase(
		val event: BudgetUsageChangedEvent,
		val expectedType: NotificationType?,
		val expectedCrossedPolicyKeys: List<String>? = null,
	)

	companion object {
		private val OCCURRED_AT = Instant.parse("2026-09-02T00:00:00Z")

		@JvmStatic
		fun budgetTransitions(): Stream<BudgetCase> {
			val id = UUID.fromString("11111111-1111-1111-1111-111111111111")
			fun event(previousUsed: Long, currentUsed: Long, budget: Long) = BudgetUsageChangedEvent(
				id, budget, previousUsed, budget, currentUsed, OCCURRED_AT,
			)
			val huge = Long.MAX_VALUE / 2
			val hugeSeventyNinePercent = BigInteger.valueOf(huge)
				.multiply(BigInteger.valueOf(79))
				.divide(BigInteger.valueOf(100))
				.longValueExact()
			return Stream.of(
				BudgetCase(event(79, 80, 100), NotificationType.BUDGET_WARNING),
				BudgetCase(event(99, 100, 100), NotificationType.BUDGET_EXCEEDED),
				BudgetCase(event(70, 105, 100), NotificationType.BUDGET_EXCEEDED),
				BudgetCase(event(80, 80, 100), null),
				BudgetCase(event(105, 70, 100), null),
				BudgetCase(event(0, 1, 0), null),
				BudgetCase(
					event(hugeSeventyNinePercent, huge, huge),
					NotificationType.BUDGET_EXCEEDED,
					listOf("BUDGET:$id:80", "BUDGET:$id:100"),
				),
			)
		}
	}
}
