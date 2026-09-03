package com.knowave.cashboard.domains.notification.controller

import com.knowave.cashboard.domains.notification.service.NotificationCommandService
import com.knowave.cashboard.domains.notification.service.NotificationQueryService
import com.knowave.cashboard.domains.notification.service.dto.NotificationPageResult
import com.knowave.cashboard.domains.notification.service.dto.NotificationResult
import com.knowave.cashboard.common.exception.InvalidNotificationPageException
import com.knowave.cashboard.common.exception.NotificationNotFoundException
import com.knowave.cashboard.domains.notification.entity.NotificationStatus
import com.knowave.cashboard.domains.notification.entity.NotificationType
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID
import org.mockito.BDDMockito.given
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(NotificationController::class)
class NotificationControllerTest {
    @Autowired lateinit var mockMvc: MockMvc
    @MockitoBean lateinit var queryService: NotificationQueryService
    @MockitoBean lateinit var commandService: NotificationCommandService

    @Test
    fun `알림 목록은 페이지와 읽음 필터를 반환한다`() {
        given(queryService.getPage(0, 20, false)).willReturn(
            NotificationPageResult(emptyList(), 0, 20, 0, 0, false),
        )
        mockMvc.perform(get("/notifications").param("page", "0").param("size", "20").param("read", "false"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.page").value(0))
            .andExpect(jsonPath("$.data.hasNext").value(false))
    }

    @Test
    fun `페이지 크기가 100을 초과하면 잘못된 페이지 오류를 반환한다`() {
        given(queryService.getPage(0, 101, null)).willThrow(
            InvalidNotificationPageException("size must be between 1 and 100."),
        )
        mockMvc.perform(get("/notifications").param("size", "101"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.data.code").value("INVALID_NOTIFICATION_PAGE"))
    }

    @Test
    fun `음수 페이지는 잘못된 페이지 오류를 반환한다`() {
        given(queryService.getPage(-1, 20, null)).willThrow(InvalidNotificationPageException("invalid page"))
        mockMvc.perform(get("/notifications").param("page", "-1"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.data.code").value("INVALID_NOTIFICATION_PAGE"))
    }

    @Test
    fun `알림 상세 조회는 성공 envelope와 상세 데이터를 반환한다`() {
        given(queryService.get(notificationId)).willReturn(notificationResult())
        mockMvc.perform(get("/notifications/{id}", notificationId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(notificationId.toString()))
            .andExpect(jsonPath("$.data.type").value("PAYMENT_DUE"))
    }

    @Test
    fun `미읽음 개수 조회는 개수를 반환한다`() {
        given(queryService.countUnread()).willReturn(3L)
        mockMvc.perform(get("/notifications/unread-count"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.count").value(3))
    }

    @Test
    fun `개별 읽음 처리는 반환된 알림을 envelope로 감싼다`() {
        given(commandService.markRead(notificationId)).willReturn(notificationResult(readAt = Instant.parse("2026-09-03T01:00:00Z")))
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/notifications/{id}/read", notificationId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.readAt").exists())
    }

    @Test
    fun `전체 읽음 처리는 처리 건수를 반환한다`() {
        given(commandService.markAllRead()).willReturn(3)
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/notifications/read-all"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.count").value(3))
    }

    @Test
    fun `존재하지 않는 알림 상세 조회는 404를 반환한다`() {
        given(queryService.get(notificationId)).willThrow(NotificationNotFoundException(notificationId))
        mockMvc.perform(get("/notifications/{id}", notificationId))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.data.code").value("NOTIFICATION_NOT_FOUND"))
    }

    @Test
    fun `잘못된 UUID 경로는 400을 반환한다`() {
        mockMvc.perform(get("/notifications/not-a-uuid"))
            .andExpect(status().isBadRequest)
    }

    private val notificationId = UUID.fromString("00000000-0000-0000-0000-000000000001")

    private fun notificationResult(readAt: Instant? = null) = NotificationResult(
        notificationId, NotificationType.PAYMENT_DUE, "결제 예정", "결제가 예정되어 있습니다.",
        NotificationStatus.PENDING, Instant.parse("2026-09-03T00:00:00Z"), null, readAt,
        LocalDateTime.of(2026, 9, 3, 10, 0), LocalDateTime.of(2026, 9, 3, 10, 0),
    )
}
