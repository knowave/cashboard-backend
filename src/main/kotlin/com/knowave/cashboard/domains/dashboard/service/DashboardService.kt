package com.knowave.cashboard.domains.dashboard.service

import com.knowave.cashboard.domains.dashboard.service.dto.DashboardResult

interface DashboardService {
	fun getDashboard(): DashboardResult
}
