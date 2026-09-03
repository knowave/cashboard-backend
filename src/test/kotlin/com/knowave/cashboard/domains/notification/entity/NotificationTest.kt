package com.knowave.cashboard.domains.notification.entity

import com.knowave.cashboard.common.exception.InvalidEnumValueException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.Instant

class NotificationTest {
	@Test
	fun `알림을 생성하면 enum name과 PENDING을 저장한다`() {
		val notification = notification()

		assertThat(notification.type).isEqualTo("PAYMENT_DUE")
		assertThat(notification.status).isEqualTo("PENDING")
	}

	@Test
	fun `읽음 처리는 최초 시각을 유지한다`() {
		val notification = notification()
		val first = Instant.parse("2026-09-14T01:00:00Z")

		notification.markRead(first)
		notification.markRead(first.plusSeconds(60))

		assertThat(notification.readAt).isEqualTo(first)
	}

	@Test
	fun `문자열 enum은 공백과 대소문자를 정규화한다`() {
		assertThat(NotificationType.from(" payment_due ")).isEqualTo(NotificationType.PAYMENT_DUE)
		assertThat(NotificationStatus.from(" sent ")).isEqualTo(NotificationStatus.SENT)
	}

	@Test
	fun `알 수 없는 문자열 enum은 거절한다`() {
		assertThatThrownBy { NotificationType.from("UNKNOWN") }
			.isInstanceOf(InvalidEnumValueException::class.java)
	}

	private fun notification() = Notification.create(
		type = NotificationType.PAYMENT_DUE,
		title = "결제 예정",
		message = "내일 카드대금 520,000원이 예정되어 있어요.",
		scheduledAt = Instant.parse("2026-09-14T00:00:00Z"),
		deduplicationKey = "PAYMENT_DUE:schedule:2026-09-15:1",
	)
}
