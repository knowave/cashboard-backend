package com.knowave.cashboard.domains.notification.service

import com.knowave.cashboard.common.exception.InvalidNotificationPageException
import com.knowave.cashboard.common.exception.InvalidEnumValueException
import com.knowave.cashboard.common.exception.NotificationDataIntegrityException
import com.knowave.cashboard.common.exception.NotificationNotFoundException
import com.knowave.cashboard.domains.notification.entity.Notification
import com.knowave.cashboard.domains.notification.entity.NotificationType
import com.knowave.cashboard.domains.notification.repository.NewNotification
import com.knowave.cashboard.domains.notification.repository.ConditionalReadResult
import com.knowave.cashboard.domains.notification.repository.NotificationPreferenceRepository
import com.knowave.cashboard.domains.notification.repository.NotificationRepository
import com.knowave.cashboard.domains.notification.repository.NotificationSettingRepository
import com.knowave.cashboard.domains.notification.service.dto.NotificationSettingCommand
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class NotificationServiceTest {
	private val notificationRepository = FakeNotificationRepository()
	private val settingRepository = FakeNotificationSettingRepository()
	private val preferenceRepository = FakeNotificationPreferenceRepository()
	private val generationService = NotificationGenerationServiceImpl(notificationRepository, settingRepository)
	private val queryService = NotificationQueryServiceImpl(notificationRepository)
	private val commandService = NotificationCommandServiceImpl(
		notificationRepository,
		Clock.fixed(Instant.parse("2026-09-02T00:00:00Z"), ZoneOffset.UTC),
	)
	private val settingService = NotificationSettingServiceImpl(settingRepository, preferenceRepository)

	@Test
	fun `설정이 없으면 후보 알림을 생성한다`() {
		val created = generationService.createIfEnabled(paymentCandidate("default-enabled"))

		assertThat(created).isTrue()
		assertThat(notificationRepository.inserted).hasSize(1)
	}

	@Test
	fun `비활성 유형은 후보 알림을 생성하지 않는다`() {
		settingRepository.upsert(NotificationType.PAYMENT_DUE, false)

		val created = generationService.createIfEnabled(paymentCandidate("disabled"))

		assertThat(created).isFalse()
		assertThat(notificationRepository.inserted).isEmpty()
	}

	@Test
	fun `동일 중복 키는 생성하지 않는다`() {
		assertThat(generationService.createIfEnabled(paymentCandidate("same-key"))).isTrue()

		assertThat(generationService.createIfEnabled(paymentCandidate("same-key"))).isFalse()
		assertThat(notificationRepository.inserted).hasSize(1)
	}

	@Test
	fun `목록 페이지는 음수 페이지를 거절한다`() {
		assertThatThrownBy { queryService.getPage(page = -1, size = 20, read = null) }
			.isInstanceOf(InvalidNotificationPageException::class.java)
	}

	@Test
	fun `목록 페이지는 100을 넘는 크기를 거절한다`() {
		assertThatThrownBy { queryService.getPage(page = 0, size = 101, read = null) }
			.isInstanceOf(InvalidNotificationPageException::class.java)
	}

	@Test
	fun `전체 읽음은 이번 요청에서 변경된 알림 수를 반환한다`() {
		notificationRepository.markAllReadResult = 2

		assertThat(commandService.markAllRead()).isEqualTo(2)
		assertThat(commandService.markAllRead()).isZero()
	}

	@Test
	fun `개별 읽음은 최초 시각을 유지하고 재요청에도 최신 알림을 반환한다`() {
		val id = UUID.fromString("00000000-0000-0000-0000-000000000002")
		notificationRepository.notifications[id] = notification(id, "atomic-read")

		val first = commandService.markRead(id)
		val repeated = commandService.markRead(id)

		assertThat(first.readAt).isEqualTo(Instant.parse("2026-09-02T00:00:00Z"))
		assertThat(repeated.readAt).isEqualTo(first.readAt)
		assertThat(notificationRepository.notifications.getValue(id).readAt).isEqualTo(first.readAt)
	}

	@Test
	fun `존재하지 않는 알림 읽음은 not found 오류를 반환한다`() {
		val id = UUID.fromString("00000000-0000-0000-0000-000000000003")

		assertThatThrownBy { commandService.markRead(id) }
			.isInstanceOf(NotificationNotFoundException::class.java)
	}

	@Test
	fun `설정 부분 변경은 전달되지 않은 유형과 push 설정을 유지한다`() {
		settingRepository.upsert(NotificationType.PAYMENT_DUE, false)
		preferenceRepository.pushEnabled = false

		val result = settingService.patchSettings(
			NotificationSettingCommand(settings = mapOf(NotificationType.WEEKLY_REPORT to false)),
		)

		assertThat(result.pushEnabled).isFalse()
		assertThat(result.settings.getValue("PAYMENT_DUE")).isFalse()
		assertThat(result.settings.getValue("WEEKLY_REPORT")).isFalse()
		assertThat(result.settings.getValue("MONTHLY_REPORT")).isTrue()
	}

	@Test
	fun `push 설정만 변경해도 유형별 설정은 기본 활성 상태를 유지한다`() {
		val result = settingService.patchSettings(NotificationSettingCommand(pushEnabled = false))

		assertThat(result.pushEnabled).isFalse()
		assertThat(result.settings.values).allMatch { it }
	}

	@Test
	fun `잘못 저장된 알림 유형은 원인을 보존한 데이터 무결성 오류로 변환한다`() {
		val notification = Notification.create(
			type = NotificationType.PAYMENT_DUE,
			title = "결제 예정",
			message = "내일 결제 예정입니다.",
			scheduledAt = Instant.parse("2026-09-02T00:00:00Z"),
			deduplicationKey = "corrupted-type",
		).withPersistedField("id", UUID.fromString("00000000-0000-0000-0000-000000000001"))
			.withPersistedField("type", "UNKNOWN_TYPE")

		assertThatThrownBy { notification.toResult() }
			.isInstanceOf(NotificationDataIntegrityException::class.java)
			.hasMessageContaining("field=type")
			.hasCauseInstanceOf(InvalidEnumValueException::class.java)
	}

	private fun paymentCandidate(key: String) = NewNotification(
		id = UUID.randomUUID(),
		type = NotificationType.PAYMENT_DUE,
		title = "결제 예정",
		message = "내일 결제 예정입니다.",
		scheduledAt = Instant.parse("2026-09-02T00:00:00Z"),
		deduplicationKey = key,
	)

	private fun notification(id: UUID, key: String) = Notification.create(
		type = NotificationType.PAYMENT_DUE,
		title = "결제 예정",
		message = "내일 결제 예정입니다.",
		scheduledAt = Instant.parse("2026-09-02T00:00:00Z"),
		deduplicationKey = key,
	).withPersistedField("id", id)
}

private fun Notification.withPersistedField(name: String, value: Any): Notification {
	var type: Class<*>? = javaClass
	while (type != null) {
		val field = runCatching { type.getDeclaredField(name) }.getOrNull()
		if (field != null) {
			field.isAccessible = true
			field.set(this, value)
			return this
		}
		type = type.superclass
	}
	error("필드를 찾을 수 없습니다. name=$name")
}

private class FakeNotificationRepository : NotificationRepository {
	val inserted = mutableListOf<NewNotification>()
	val notifications = mutableMapOf<UUID, Notification>()
	var markAllReadResult = 0

	override fun insertIfAbsent(candidate: NewNotification): Boolean {
		if (inserted.any { it.deduplicationKey == candidate.deduplicationKey }) return false
		inserted += candidate
		return true
	}

	override fun findById(id: UUID): Notification? = null
	override fun findPage(read: Boolean?, pageable: Pageable): Page<Notification> = Page.empty(pageable)
	override fun countUnread(): Long = 0
	override fun save(notification: Notification): Notification = error("개별 읽음은 원자 저장소 연산을 사용해야 합니다.")
	override fun markReadIfUnread(id: UUID, now: Instant): ConditionalReadResult? {
		val notification = notifications[id] ?: return null
		val changed = notification.readAt == null
		notification.markRead(now)
		return ConditionalReadResult(notification, changed)
	}
	override fun markAllRead(now: Instant): Int = markAllReadResult.also { markAllReadResult = 0 }
}

private class FakeNotificationSettingRepository : NotificationSettingRepository {
	private val values = mutableMapOf<NotificationType, Boolean>()

	override fun isEnabled(type: NotificationType, defaultValue: Boolean): Boolean = values[type] ?: defaultValue
	override fun upsert(type: NotificationType, enabled: Boolean) {
		values[type] = enabled
	}
	override fun findAll(): Map<NotificationType, Boolean> = values.toMap()
}

private class FakeNotificationPreferenceRepository : NotificationPreferenceRepository {
	var pushEnabled = true

	override fun getPushEnabled(defaultValue: Boolean): Boolean = pushEnabled
	override fun upsertPushEnabled(enabled: Boolean) {
		pushEnabled = enabled
	}
}
