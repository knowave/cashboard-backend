package com.knowave.cashboard.domains.dashboard.controller

import com.knowave.cashboard.common.response.ApiResponse
import com.knowave.cashboard.common.response.success
import com.knowave.cashboard.common.security.AuthenticatedUser
import com.knowave.cashboard.domains.dashboard.controller.dto.DashboardResponse
import com.knowave.cashboard.domains.dashboard.controller.dto.toResponse
import com.knowave.cashboard.domains.dashboard.service.DashboardService
import java.util.UUID
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/dashboard")
class DashboardController(
	private val dashboardService: DashboardService,
) {
	@GetMapping
	fun getDashboard(@AuthenticationPrincipal user: AuthenticatedUser): ApiResponse<DashboardResponse> =
		success(dashboardService.getDashboard(user.userId).toResponse())

}
