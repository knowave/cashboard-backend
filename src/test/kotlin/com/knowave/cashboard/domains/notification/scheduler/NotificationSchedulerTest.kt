package com.knowave.cashboard.domains.notification.scheduler

import com.knowave.cashboard.domains.notification.job.ScheduledNotificationJob
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.DateTimeException
import kotlin.test.assertEquals

class NotificationSchedulerTest {
    @Test
    fun `한 Job 실패 후에도 나머지 Job을 실행한다`() {
        val failing = mock(ScheduledNotificationJob::class.java)
        val payment = mock(ScheduledNotificationJob::class.java)
        val report = mock(ScheduledNotificationJob::class.java)
        val fourth = mock(ScheduledNotificationJob::class.java)
        org.mockito.Mockito.`when`(failing.name).thenReturn("failing")
        val expectedContext = NotificationScheduleContext(java.time.LocalDate.of(2026, 9, 2), Instant.parse("2026-09-02T00:00:00Z"), ZoneId.of("Asia/Seoul"))
        org.mockito.Mockito.doThrow(IllegalStateException("boom")).`when`(failing).run(expectedContext)
        listOf(payment, report, fourth).forEachIndexed { i, job -> org.mockito.Mockito.`when`(job.name).thenReturn("job-$i") }
        val scheduler = NotificationScheduler(
            listOf(failing, payment, report, fourth),
            Clock.fixed(Instant.parse("2026-09-02T00:00:00Z"), ZoneId.of("UTC")),
            "Asia/Seoul",
        )

        scheduler.runOnce()

        verify(failing).run(NotificationScheduleContext(java.time.LocalDate.of(2026, 9, 2), Instant.parse("2026-09-02T00:00:00Z"), ZoneId.of("Asia/Seoul")))
        verify(payment).run(expectedContext)
        verify(report).run(expectedContext)
        verify(fourth).run(expectedContext)
    }

    @Test
    fun `고정 시각에서 서울 오전 9시 scheduledAt을 계산한다`() {
        val scheduler = NotificationScheduler(emptyList(), Clock.fixed(Instant.parse("2026-09-02T00:00:00Z"), ZoneId.of("UTC")), "Asia/Seoul")
        val context = scheduler.context()
        assertEquals(Instant.parse("2026-09-02T00:00:00Z"), context.scheduledAt)
        assertEquals(java.time.LocalDate.of(2026, 9, 2), context.date)
    }

    @Test
    fun `잘못된 시간대 설정은 생성 시 즉시 실패한다`() {
        kotlin.test.assertFailsWith<DateTimeException> {
            NotificationScheduler(emptyList(), Clock.systemUTC(), "Invalid/Zone")
        }
    }
}
