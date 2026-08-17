#!/usr/bin/env bash
set -euo pipefail

# 1) 서버 기동 대기 (고정 sleep 대신 readiness 폴링 — 레이스 방지). 타임아웃 + 실패 시 프로세스 정리 포함
./gradlew bootRun & BOOT_PID=$!
trap 'kill $BOOT_PID 2>/dev/null || true' EXIT
for _ in $(seq 1 60); do curl -sf localhost:8080/api/api-docs >/dev/null 2>&1 && break; sleep 1; done
curl -sf localhost:8080/api/api-docs >/dev/null || { echo "서버 기동 실패"; exit 1; }
# 주의: readiness 폴링 경로도 server.servlet.context-path: /api가 적용된다(application.yaml:19-21) — /api-docs가 아니라 /api/api-docs

# 2) 시딩: 월별 예산 1건(이미 존재하면 조회로 대체) + 6개월에 걸친 지출(카테고리 null 1건 포함)
MB_ID=$(curl -s -X POST localhost:8080/api/monthly-budgets -H 'Content-Type: application/json' \
  -d '{"targetMonth":"2026-08","monthlyBudget":1000000}' | jq -r '.data.id // empty')
if [ -z "$MB_ID" ] || [ "$MB_ID" = "null" ]; then
  # 재실행 시 DuplicateMonthlyBudgetException으로 위 생성이 실패할 수 있음 → 기존 것을 조회해 사용
  MB_ID=$(curl -s "localhost:8080/api/monthly-budgets/2026-08" | jq -r '.data.id // empty')
fi
[ -n "$MB_ID" ] || { echo "monthly budget id를 확보하지 못함"; exit 1; }
curl -s -X POST "localhost:8080/api/monthly-budgets/$MB_ID/expenses" -H 'Content-Type: application/json' \
  -d '{"amount":320000,"category":"FOOD","spentAt":"2026-08-05"}'
curl -s -X POST "localhost:8080/api/monthly-budgets/$MB_ID/expenses" -H 'Content-Type: application/json' \
  -d '{"amount":150000,"spentAt":"2026-08-10"}'   # category 없음 → UNCATEGORIZED 확인용
# ... 나머지 3~7월 지출도 동일한 방식으로 각 1건 이상 추가(연도 경계 테스트를 원하면 2025-12/2025-11도 추가)
# 주의: addExpense는 spentAt과 targetMonth의 일치 여부를 검증하지 않으므로 다른 달 지출을 이 예산 밑에 추가해도 API 호출은 되지만
#      해당 monthly_budget의 usedAmount가 함께 누적된다(분석 API 자체엔 영향 없음, 예산 조회 화면에서만 티가 남).

# 3) 실제 분석 API 호출 및 확인 (출력 + UNCATEGORIZED 존재 여부를 실제 assertion으로 확인 — "출력만 하고 사람이 눈으로 확인"에 그치지 않도록)
RESULT=$(curl -s "localhost:8080/api/expense-analysis?year=2026&month=8")
echo "$RESULT" | jq
echo "$RESULT" | jq -e '.data.categories[] | select(.category == "UNCATEGORIZED")' >/dev/null \
  || { echo "FAIL: UNCATEGORIZED 버킷 없음"; exit 1; }
#   - categories가 amount 내림차순인지, trend가 yearMonth 오름차순이고 데이터 없는 월이 생략되는지는 위 jq 출력을 눈으로 확인

# 4) 빈 월 확인 (totalExpense=0, categories=[] 를 실제 assertion으로 확인)
EMPTY_RESULT=$(curl -s "localhost:8080/api/expense-analysis?year=2020&month=1")
[ "$(echo "$EMPTY_RESULT" | jq '.data.totalExpense')" = "0" ] || { echo "FAIL: 빈 월 totalExpense != 0"; exit 1; }
[ "$(echo "$EMPTY_RESULT" | jq -c '.data.categories')" = "[]" ] || { echo "FAIL: 빈 월 categories != []"; exit 1; }

# 5) 에러 케이스 확인 (모두 400이어야 함 — 실제로 검사해서 아니면 스크립트가 실패하도록)
check_400() {
  CODE=$(curl -s -o /dev/null -w "%{http_code}" "localhost:8080/api/expense-analysis?$1")
  [ "$CODE" = "400" ] || { echo "FAIL: $1 -> $CODE (400 기대)"; exit 1; }
}
check_400 "month=8"                 # year 누락
check_400 "year=abc&month=8"        # year 타입 오류
check_400 "year=2026&month=13"      # month 범위 위반

echo "스모크 테스트 전부 통과"
