package com.knowave.cashboard.domains.notification.controller

import com.knowave.cashboard.common.response.success
import com.knowave.cashboard.domains.notification.controller.dto.ReadAllResponse
import com.knowave.cashboard.domains.notification.controller.dto.UnreadCountResponse
import com.knowave.cashboard.domains.notification.controller.dto.toResponse
import com.knowave.cashboard.domains.notification.service.NotificationCommandService
import com.knowave.cashboard.domains.notification.service.NotificationQueryService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/notifications")
class NotificationController(
    private val queryService: NotificationQueryService,
    private val commandService: NotificationCommandService,
) {
    @GetMapping
    fun getPage(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) read: Boolean?,
    ) = success(queryService.getPage(page, size, read).toResponse())

    @GetMapping("/unread-count")
    fun unreadCount() = success(UnreadCountResponse(queryService.countUnread()))

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID) = success(queryService.get(id).toResponse())

    @PatchMapping("/{id}/read")
    fun read(@PathVariable id: UUID) = success(commandService.markRead(id).toResponse())

    @PatchMapping("/read-all")
    fun readAll() = success(ReadAllResponse(commandService.markAllRead()))
}
