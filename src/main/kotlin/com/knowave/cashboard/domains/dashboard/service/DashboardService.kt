package com.knowave.cashboard.domains.dashboard.service

import com.knowave.cashboard.domains.dashboard.service.dto.DashboardResult
import java.util.UUID

interface DashboardService {
	fun getDashboard(userId: UUID): DashboardResult
}
