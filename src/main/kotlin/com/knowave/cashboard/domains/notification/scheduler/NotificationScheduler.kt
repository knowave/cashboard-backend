package com.knowave.cashboard.domains.notification.scheduler

import com.knowave.cashboard.domains.notification.job.ScheduledNotificationJob
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

@Component
class NotificationScheduler(
    private val jobs: List<ScheduledNotificationJob>,
    private val clock: Clock,
    @Value("\${notification.scheduler.zone:Asia/Seoul}") zoneId: String,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val zone: ZoneId = ZoneId.of(zoneId)

    @Scheduled(cron = "\${notification.scheduler.cron:0 0 9 * * *}", zone = "\${notification.scheduler.zone:Asia/Seoul}")
    fun runOnce() {
        val context = context()
        jobs.forEach { job ->
            runCatching { job.run(context) }
                .onFailure { logger.error("Scheduled notification job failed. job={}, date={}", job.name, context.date, it) }
        }
    }

    fun context(): NotificationScheduleContext {
        val date = LocalDate.now(clock.withZone(zone))
        val scheduledAt = date.atTime(9, 0).atZone(zone).toInstant()
        return NotificationScheduleContext(date, scheduledAt, zone)
    }
}
