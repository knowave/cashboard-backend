package com.knowave.cashboard.domains.notification.controller

import com.knowave.cashboard.domains.notification.service.NotificationSettingService
import com.knowave.cashboard.domains.notification.service.dto.NotificationSettingResult
import com.knowave.cashboard.domains.notification.service.dto.NotificationSettingCommand
import org.junit.jupiter.api.Test
import com.knowave.cashboard.support.WithAuthenticatedUser
import com.knowave.cashboard.common.config.ClockConfig
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
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
import java.util.UUID

// ponytail: 슬라이스 테스트는 매핑·검증을 검증한다. 보안 체인은 전용 테스트의 몫이므로
// 필터를 끈다 — @WebMvcTest는 우리 SecurityConfig를 로드하지 않고 Boot 기본 설정을
// 쓰기 때문에 CSRF가 켜져 비-GET이 403을 받는다. @AuthenticationPrincipal은
// SecurityContextHolder에서 해석되므로 필터를 꺼도 @WithAuthenticatedUser가 동작한다.
@AutoConfigureMockMvc(addFilters = false)
@WithAuthenticatedUser
@Import(ClockConfig::class)
@WebMvcTest(NotificationSettingController::class)
class NotificationSettingControllerTest {
    @Autowired lateinit var mockMvc: MockMvc
    @MockitoBean lateinit var settingService: NotificationSettingService

    // @AuthenticationPrincipal AuthenticatedUser.userId와 동일한 placeholder.
    private val tempUserId = UUID.fromString("00000000-0000-0000-0000-000000000000")

    @Test
    fun `설정 PATCH는 전역 push와 유형별 설정을 반환한다`() {
        given(settingService.patchSettings(eqValue(tempUserId), anyValue(NotificationSettingCommand()))).willReturn(
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
        given(settingService.getSettings(tempUserId)).willReturn(NotificationSettingResult(true, mapOf("PAYMENT_DUE" to true)))
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/notification-settings"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.pushEnabled").value(true))
            .andExpect(jsonPath("$.data.settings.PAYMENT_DUE").value(true))
    }

    @Test
    fun `pushEnabled-only PATCH는 유형 설정을 생략해 null로 전달한다`() {
        given(settingService.patchSettings(eqValue(tempUserId), anyValue(NotificationSettingCommand()))).willReturn(NotificationSettingResult(false, emptyMap()))
        mockMvc.perform(patch("/notification-settings").contentType(MediaType.APPLICATION_JSON).content("""{"pushEnabled":false}"""))
            .andExpect(status().isOk)
        val captor = ArgumentCaptor.forClass(NotificationSettingCommand::class.java)
        then(settingService).should().patchSettings(eqValue(tempUserId), captureValue(captor, NotificationSettingCommand()))
        check(captor.value.pushEnabled == false && captor.value.settings == null)
    }

    @Test
    fun `settings-only PATCH는 push 설정을 생략해 null로 전달한다`() {
        given(settingService.patchSettings(eqValue(tempUserId), anyValue(NotificationSettingCommand()))).willReturn(NotificationSettingResult(true, mapOf("PAYMENT_DUE" to false)))
        mockMvc.perform(patch("/notification-settings").contentType(MediaType.APPLICATION_JSON).content("""{"settings":{"PAYMENT_DUE":false}}"""))
            .andExpect(status().isOk)
        val captor = ArgumentCaptor.forClass(NotificationSettingCommand::class.java)
        then(settingService).should().patchSettings(eqValue(tempUserId), captureValue(captor, NotificationSettingCommand()))
        check(captor.value.pushEnabled == null && captor.value.settings == mapOf(NotificationType.PAYMENT_DUE to false))
    }

    private fun <T> anyValue(fallback: T): T = any<T>() ?: fallback
    private fun <T> eqValue(value: T): T = org.mockito.ArgumentMatchers.eq(value) ?: value
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
