package com.knowave.cashboard.domains.notification.scheduler

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class NotificationScheduleContext(val date: LocalDate, val scheduledAt: Instant, val zoneId: ZoneId)
