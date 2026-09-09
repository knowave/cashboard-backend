package com.knowave.cashboard.domains.notification.service

import com.knowave.cashboard.domains.notification.entity.NotificationType
import com.knowave.cashboard.domains.notification.policy.ThresholdNotificationDecision
import com.knowave.cashboard.domains.notification.repository.NewNotification
import com.knowave.cashboard.domains.notification.repository.NotificationPolicyMarkerRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class ThresholdNotificationServiceTest {
	private val markerRepository = FakeMarkerRepository()
	private val generationService = FakeGenerationService()
	private val service = ThresholdNotificationServiceImpl(markerRepository, generationService)
	private val occurredAt = Instant.parse("2026-09-02T00:00:00Z")
	private val userId = UUID.randomUUID()

	@Test fun `통과 marker를 모두 저장하고 선택 단계 알림 하나만 생성한다`() {
		val candidate = candidate()
		service.process(userId, ThresholdNotificationDecision(userId, listOf("BUDGET:id:80", "BUDGET:id:100"), "BUDGET:id:100", candidate), occurredAt)
		assertThat(markerRepository.keys).containsExactlyInAnyOrder("BUDGET:id:80", "BUDGET:id:100")
		assertThat(generationService.created).containsExactly(candidate)
	}

	@Test fun `같은 결정을 반복하면 두 번째 처리는 알림을 만들지 않는다`() {
		val decision = ThresholdNotificationDecision(userId, listOf("BUDGET:id:100"), "BUDGET:id:100", candidate())
		service.process(userId, decision, occurredAt); service.process(userId, decision, occurredAt)
		assertThat(generationService.created).hasSize(1)
	}

	@Test fun `선택 marker가 이미 존재하고 하위 marker만 새로 선점되면 알림을 추가 생성하지 않는다`() {
		service.process(userId, ThresholdNotificationDecision(userId, listOf("BUDGET:id:100"), "BUDGET:id:100", candidate()), occurredAt)
		service.process(userId, ThresholdNotificationDecision(userId, listOf("BUDGET:id:80", "BUDGET:id:100"), "BUDGET:id:100", candidate()), occurredAt)
		assertThat(markerRepository.keys).containsExactlyInAnyOrder("BUDGET:id:80", "BUDGET:id:100")
		assertThat(generationService.created).hasSize(1)
	}

	@Test fun `불완전 결정은 marker와 알림 생성을 모두 수행하지 않는다`() {
		service.process(userId, ThresholdNotificationDecision(userId, emptyList(), null, null), occurredAt)
		assertThat(markerRepository.keys).isEmpty()
		assertThat(generationService.created).isEmpty()
	}

	@Test fun `유형이 비활성이어도 marker는 남는다`() {
		generationService.enabled = false
		service.process(userId, ThresholdNotificationDecision(userId, listOf("BUDGET:id:80"), "BUDGET:id:80", candidate()), occurredAt)
		assertThat(markerRepository.keys).contains("BUDGET:id:80")
		assertThat(generationService.created).isEmpty()
	}

	private fun candidate() = NewNotification(userId = userId, type = NotificationType.BUDGET_EXCEEDED, title = "title", message = "message", scheduledAt = occurredAt, deduplicationKey = "key")
}

private class FakeMarkerRepository : NotificationPolicyMarkerRepository {
	val keys = linkedSetOf<String>()
	override fun claimAll(userId: UUID, policyKeys: Set<String>, processedAt: Instant): Set<String> = policyKeys.filterTo(linkedSetOf()) { keys.add(it) }
}
private class FakeGenerationService : NotificationGenerationService {
	var enabled = true
	val created = mutableListOf<NewNotification>()
	override fun createIfEnabled(userId: UUID, candidate: NewNotification): Boolean { if (!enabled) return false; created += candidate; return true }
}
