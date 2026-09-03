package com.knowave.cashboard.domains.notification.controller

import com.knowave.cashboard.domains.notification.service.NotificationSettingService
import com.knowave.cashboard.domains.notification.service.dto.NotificationSettingResult
import com.knowave.cashboard.domains.notification.service.dto.NotificationSettingCommand
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.mockito.BDDMockito.given
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentCaptor
import org.mockito.BDDMockito.then
import com.knowave.cashboard.domains.notification.entity.NotificationType

@WebMvcTest(NotificationSettingController::class)
class NotificationSettingControllerTest {
    @Autowired lateinit var mockMvc: MockMvc
    @MockitoBean lateinit var settingService: NotificationSettingService

    @Test
    fun `설정 PATCH는 전역 push와 유형별 설정을 반환한다`() {
        given(settingService.patchSettings(anyValue(NotificationSettingCommand()))).willReturn(
            NotificationSettingResult(false, mapOf("PAYMENT_DUE" to false)),
        )
        mockMvc.perform(
            patch("/notification-settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"pushEnabled":false,"settings":{"PAYMENT_DUE":false}}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.pushEnabled").value(false))
            .andExpect(jsonPath("$.data.settings.PAYMENT_DUE").value(false))
    }

    @Test
    fun `설정 GET은 현재 전역 및 유형별 설정을 반환한다`() {
        given(settingService.getSettings()).willReturn(NotificationSettingResult(true, mapOf("PAYMENT_DUE" to true)))
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/notification-settings"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.pushEnabled").value(true))
            .andExpect(jsonPath("$.data.settings.PAYMENT_DUE").value(true))
    }

    @Test
    fun `pushEnabled-only PATCH는 유형 설정을 생략해 null로 전달한다`() {
        given(settingService.patchSettings(anyValue(NotificationSettingCommand()))).willReturn(NotificationSettingResult(false, emptyMap()))
        mockMvc.perform(patch("/notification-settings").contentType(MediaType.APPLICATION_JSON).content("""{"pushEnabled":false}"""))
            .andExpect(status().isOk)
        val captor = ArgumentCaptor.forClass(NotificationSettingCommand::class.java)
        then(settingService).should().patchSettings(captureValue(captor, NotificationSettingCommand()))
        check(captor.value.pushEnabled == false && captor.value.settings == null)
    }

    @Test
    fun `settings-only PATCH는 push 설정을 생략해 null로 전달한다`() {
        given(settingService.patchSettings(anyValue(NotificationSettingCommand()))).willReturn(NotificationSettingResult(true, mapOf("PAYMENT_DUE" to false)))
        mockMvc.perform(patch("/notification-settings").contentType(MediaType.APPLICATION_JSON).content("""{"settings":{"PAYMENT_DUE":false}}"""))
            .andExpect(status().isOk)
        val captor = ArgumentCaptor.forClass(NotificationSettingCommand::class.java)
        then(settingService).should().patchSettings(captureValue(captor, NotificationSettingCommand()))
        check(captor.value.pushEnabled == null && captor.value.settings == mapOf(NotificationType.PAYMENT_DUE to false))
    }

    private fun <T> anyValue(fallback: T): T = any<T>() ?: fallback
    private fun <T> captureValue(captor: ArgumentCaptor<T>, fallback: T): T = captor.capture() ?: fallback

    @Test
    fun `알 수 없는 알림 유형은 enum 오류를 반환한다`() {
        mockMvc.perform(
            patch("/notification-settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"settings":{"UNKNOWN":true}}"""),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.data.code").value("INVALID_ENUM_VALUE"))
    }
}
