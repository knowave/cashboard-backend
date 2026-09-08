package com.knowave.cashboard.domains.notification.job

import com.knowave.cashboard.domains.notification.scheduler.NotificationScheduleContext

interface ScheduledNotificationJob {
    val name: String
    fun run(context: NotificationScheduleContext)
}
