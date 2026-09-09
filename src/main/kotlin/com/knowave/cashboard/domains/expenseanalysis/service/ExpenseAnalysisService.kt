package com.knowave.cashboard.domains.expenseanalysis.service

import com.knowave.cashboard.domains.expenseanalysis.service.dto.ExpenseAnalysisResult
import java.util.UUID

interface ExpenseAnalysisService {
	fun getAnalysis(userId: UUID, year: Int, month: Int): ExpenseAnalysisResult
}
