package com.knowave.cashboard.common.exception

class InvalidLoanRepaymentConditionException(message: String) : CashboardException(
	errorCode = "INVALID_LOAN_REPAYMENT_CONDITION",
	message = message,
)
