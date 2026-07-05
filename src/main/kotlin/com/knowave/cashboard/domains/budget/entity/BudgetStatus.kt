package com.knowave.cashboard.domains.budget.entity

enum class BudgetStatus(
	val strategyMessage: String,
) {
	EMERGENCY("비상상태입니다. 필수 지출 외 소비를 중단하는 것이 좋습니다."),
	DANGER("위험 상태입니다. 하루 소비를 강하게 제한해야 합니다."),
	CAUTION("주의 상태입니다. 예산 기준 소비를 유지해야 합니다."),
	STABLE("안정 상태입니다. 현재 소비 흐름을 유지하세요."),
	GOOD("여유가 있습니다. 남는 금액은 비상금 또는 조기상환 검토가 가능합니다."),
}
