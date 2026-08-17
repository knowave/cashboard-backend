package com.knowave.cashboard.domains.expenseanalysis.service

import com.knowave.cashboard.domains.expenseanalysis.service.dto.ExpenseAnalysisResult

interface ExpenseAnalysisService {
	fun getAnalysis(year: Int, month: Int): ExpenseAnalysisResult
}
