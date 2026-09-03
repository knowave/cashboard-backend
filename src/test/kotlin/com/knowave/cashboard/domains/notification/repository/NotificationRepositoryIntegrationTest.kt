package com.knowave.cashboard.domains.notification.repository

import com.knowave.cashboard.domains.notification.entity.NotificationType
import com.knowave.cashboard.support.PostgreSqlIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.jdbc.core.JdbcTemplate
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.Executors

class NotificationRepositoryIntegrationTest : PostgreSqlIntegrationTest() {
	@Autowired lateinit var repository: NotificationRepository
	@Autowired lateinit var policyMarkerRepository: NotificationPolicyMarkerRepository
	@Autowired lateinit var settingRepository: NotificationSettingRepository
	@Autowired lateinit var preferenceRepository: NotificationPreferenceRepository
	@Autowired lateinit var jdbcTemplate: JdbcTemplate

	@BeforeEach
	fun clearNotificationTables() {
		jdbcTemplate.execute(
			"""
				TRUNCATE TABLE
					notifications,
					notification_policy_markers,
					notification_settings,
					notification_preferences,
					balance_shortage_states
			""".trimIndent(),
		)
	}

	@Test
	fun `같은 중복 키는 한 번만 삽입한다`() {
		val candidate = paymentCandidate("same-key")

		assertThat(repository.insertIfAbsent(candidate)).isTrue()
		assertThat(repository.insertIfAbsent(candidate.copy(id = UUID.randomUUID()))).isFalse()
		assertThat(repository.countUnread()).isEqualTo(1)
	}

	@Test
	fun `동시 중복 삽입은 한 행만 만든다`() {
		Executors.newFixedThreadPool(2).use { executor ->
			val results = listOf(1, 2)
				.map { executor.submit<Boolean> { repository.insertIfAbsent(paymentCandidate("race-key")) } }
				.map { it.get() }

			assertThat(results.count { it }).isEqualTo(1)
		}

		assertThat(repository.countUnread()).isEqualTo(1)
	}

	@Test
	fun `전체 읽음은 변경한 행 수를 반환한다`() {
		seedUnread("first")
		seedUnread("second")

		assertThat(repository.markAllRead(Instant.parse("2026-09-02T00:00:00Z"))).isEqualTo(2)
		assertThat(repository.markAllRead(Instant.parse("2026-09-02T00:01:00Z"))).isZero()
	}

	@Test
	fun `동시 개별 읽음은 한 요청만 최초 읽음 시각을 기록한다`() {
		val candidate = paymentCandidate("single-read-race")
		repository.insertIfAbsent(candidate)
		val first = Instant.parse("2026-09-02T00:00:00Z")
		val second = Instant.parse("2026-09-02T00:01:00Z")

		val results = Executors.newFixedThreadPool(2).use { executor ->
			listOf(first, second)
				.map { now -> executor.submit<ConditionalReadResult?> { repository.markReadIfUnread(candidate.id, now) } }
				.map { it.get() }
		}

		assertThat(results.filterNotNull().count { it.changed }).isEqualTo(1)
		assertThat(results.filterNotNull().count { !it.changed }).isEqualTo(1)
		assertThat(repository.findById(candidate.id)!!.readAt)
			.isEqualTo(results.first { it!!.changed }!!.notification.readAt)
		assertThat(repository.findById(candidate.id)!!.readAt).isIn(first, second)
	}

	@Test
	fun `페이지는 생성 시각과 ID 내림차순 및 읽음 상태로 조회한다`() {
		val first = paymentCandidate("page-first").copy(id = UUID(0, 1))
		val second = paymentCandidate("page-second").copy(id = UUID(0, 2))
		val read = paymentCandidate("page-read").copy(id = UUID(0, 3))
		repository.insertIfAbsent(first)
		repository.insertIfAbsent(second)
		repository.insertIfAbsent(read)
		jdbcTemplate.update(
			"UPDATE notifications SET created_at = ? WHERE id IN (?, ?)",
			LocalDateTime.of(2026, 9, 2, 9, 0),
			first.id,
			second.id,
		)
		val readNotification = repository.findById(read.id)!!
		readNotification.markRead(Instant.parse("2026-09-02T00:00:00Z"))
		repository.save(readNotification)

		assertThat(repository.findPage(false, PageRequest.of(0, 10)).content.map { it.id })
			.containsExactly(second.id, first.id)
		assertThat(repository.findPage(true, PageRequest.of(0, 10)).content.map { it.id })
			.containsExactly(read.id)
	}

	@Test
	fun `정책 marker는 새 키만 선점한다`() {
		val keys = setOf("BUDGET:one:80", "BUDGET:one:100")

		assertThat(policyMarkerRepository.claimAll(keys, Instant.parse("2026-09-02T00:00:00Z")))
			.containsExactlyInAnyOrderElementsOf(keys)
		assertThat(policyMarkerRepository.claimAll(keys, Instant.parse("2026-09-02T00:01:00Z"))).isEmpty()
	}

	@Test
	fun `설정이 없으면 활성화가 기본값이고 upsert 결과를 조회한다`() {
		assertThat(settingRepository.isEnabled(NotificationType.PAYMENT_DUE)).isTrue()

		settingRepository.upsert(NotificationType.PAYMENT_DUE, false)

		assertThat(settingRepository.isEnabled(NotificationType.PAYMENT_DUE)).isFalse()
		assertThat(settingRepository.findAll()).containsEntry(NotificationType.PAYMENT_DUE, false)
	}

	@Test
	fun `push 설정이 없으면 활성화가 기본값이고 upsert 결과를 조회한다`() {
		assertThat(preferenceRepository.getPushEnabled()).isTrue()

		preferenceRepository.upsertPushEnabled(false)

		assertThat(preferenceRepository.getPushEnabled()).isFalse()
	}

	private fun seedUnread(key: String) {
		assertThat(repository.insertIfAbsent(paymentCandidate(key))).isTrue()
	}

	private fun paymentCandidate(key: String) = NewNotification(
		type = NotificationType.PAYMENT_DUE,
		title = "결제 예정",
		message = "결제 예정 알림",
		scheduledAt = Instant.parse("2026-09-02T00:00:00Z"),
		deduplicationKey = key,
	)
}
