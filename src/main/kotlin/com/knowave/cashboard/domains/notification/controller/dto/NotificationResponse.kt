package com.knowave.cashboard.domains.notification.controller.dto

import com.knowave.cashboard.domains.notification.service.dto.NotificationPageResult
import com.knowave.cashboard.domains.notification.service.dto.NotificationResult

data class NotificationResponse(
    val id: java.util.UUID,
    val type: String,
    val title: String,
    val message: String,
    val status: String,
    val scheduledAt: java.time.Instant,
    val sentAt: java.time.Instant?,
    val readAt: java.time.Instant?,
    val createdAt: java.time.LocalDateTime?,
    val updatedAt: java.time.LocalDateTime?,
)

data class NotificationPageResponse(
    val content: List<NotificationResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
)

data class UnreadCountResponse(val count: Long)
data class ReadAllResponse(val count: Int)

fun NotificationResult.toResponse() = NotificationResponse(
    id, type.name, title, message, status.name, scheduledAt, sentAt, readAt, createdAt, updatedAt,
)

fun NotificationPageResult.toResponse() = NotificationPageResponse(
    content.map { it.toResponse() }, page, size, totalElements, totalPages, hasNext,
)
