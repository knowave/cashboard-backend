package com.knowave.cashboard.domains.notification.controller

import com.knowave.cashboard.common.response.success
import com.knowave.cashboard.common.security.AuthenticatedUser
import com.knowave.cashboard.domains.notification.controller.dto.ReadAllResponse
import com.knowave.cashboard.domains.notification.controller.dto.UnreadCountResponse
import com.knowave.cashboard.domains.notification.controller.dto.toResponse
import com.knowave.cashboard.domains.notification.service.NotificationCommandService
import com.knowave.cashboard.domains.notification.service.NotificationQueryService
import java.util.UUID
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/notifications")
class NotificationController(
    private val queryService: NotificationQueryService,
    private val commandService: NotificationCommandService,
) {
    @GetMapping
    fun getPage(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) read: Boolean?,
    ) = success(queryService.getPage(user.userId, page, size, read).toResponse())

    @GetMapping("/unread-count")
    fun unreadCount(@AuthenticationPrincipal user: AuthenticatedUser) =
        success(UnreadCountResponse(queryService.countUnread(user.userId)))

    @GetMapping("/{id}")
    fun get(@AuthenticationPrincipal user: AuthenticatedUser, @PathVariable id: UUID) =
        success(queryService.get(user.userId, id).toResponse())

    @PatchMapping("/{id}/read")
    fun read(@AuthenticationPrincipal user: AuthenticatedUser, @PathVariable id: UUID) =
        success(commandService.markRead(user.userId, id).toResponse())

    @PatchMapping("/read-all")
    fun readAll(@AuthenticationPrincipal user: AuthenticatedUser) =
        success(ReadAllResponse(commandService.markAllRead(user.userId)))
}
