package com.knowave.cashboard.domains.notification.controller

import com.knowave.cashboard.common.response.success
import com.knowave.cashboard.domains.notification.controller.dto.NotificationSettingPatchRequest
import com.knowave.cashboard.domains.notification.controller.dto.toResponse
import com.knowave.cashboard.domains.notification.service.NotificationSettingService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/notification-settings")
class NotificationSettingController(
    private val settingService: NotificationSettingService,
) {
    @GetMapping
    fun get() = success(settingService.getSettings().toResponse())

    @PatchMapping
    fun patch(@RequestBody request: NotificationSettingPatchRequest) =
        success(settingService.patchSettings(request.toCommand()).toResponse())
}
