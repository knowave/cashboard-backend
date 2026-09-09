#!/usr/bin/env bash
# Repository 인터페이스 메서드 중 userId를 받지 않는 것을 찾는다 (Phase 6 AC-20c 게이트).
#
# 단순 grep으로는 안 되는 이유: Kotlin 시그니처가 여러 줄로 쪼개지면
#   fun findCategoryExpenses(
#       @Param("userId") userId: UUID,
#       ...
# 처럼 `fun` 줄에 userId가 없어 이미 올바른 메서드가 거짓 양성으로 걸린다.
# 괄호 균형을 세어 시그니처 전체를 하나로 모아 판정한다.
#
# 판정 대상: 인터페이스 선언(`^\s*fun`)만. `override fun`은 제외되므로 Impl 중복이 안 낀다.
# save/delete는 엔티티를 받아 소유권이 엔티티에 실려 가므로 대상이 아니다.
#
# 제외 도메인 (Stage 2에서 추가): domains/user, domains/auth
#   이 둘은 "사용자가 소유한 리소스"가 아니라 **신원 자체**를 정의하는 도메인이다.
#   - domains/user: User가 곧 사용자다. findById(id)의 id가 userId이므로 소유자 파라미터가 없다.
#   - domains/auth: RefreshToken은 tokenHash가 자격 증명이다. existsByTokenHash/deleteExpiredBefore가
#     userId를 받을 이유가 없고, 받게 만들면 오히려 자격 증명 검증을 우회할 여지가 생긴다.
#   게이트의 목적은 "금융 데이터가 사용자별로 격리되는가"이고 이 두 도메인은 그 대상이 아니다.
#
# ALLOWLIST: 같은 근거(소유권이 파라미터 객체에 실려 감)로 예외 처리하는 메서드.
# 새 항목을 추가할 때는 반드시 "userId를 싣고 오는 타입"임을 확인하고 이유를 적어라.
#   - insertIfAbsent(candidate: NewNotification)
#     NewNotification data class가 userId 필드를 갖는다(NotificationRepository.kt).
#     별도 userId 파라미터를 받으면 두 출처가 어긋날 수 있어 오히려 나쁘다.
#
# 사용법: ./scripts/check-user-scope.sh
# 종료 코드: 0 = 잔여 없음, 1 = 잔여 있음
set -uo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TARGET="$ROOT/src/main/kotlin/com/knowave/cashboard/domains"

RESULT="$(
  find "$TARGET" \
      -type d \( -name user -o -name auth \) -prune -o \
      -name '*Repository.kt' -print0 \
    | xargs -0 awk '
        function bal(s,  i, c, d) {
          d = 0
          for (i = 1; i <= length(s); i++) {
            c = substr(s, i, 1)
            if (c == "(") d++
            else if (c == ")") d--
          }
          return d
        }
        /^[[:space:]]*fun (find|exists|count|mark|claim|upsert|is|get|transition|insert|acquire)/ {
          sig = $0; start = FNR; d = bal($0)
          while (d > 0 && (getline line) > 0) { sig = sig " " line; d += bal(line) }
          if (sig ~ /userId/) next
          # ALLOWLIST (파일 상단 주석의 근거 참조)
          if (sig ~ /fun insertIfAbsent\(candidate: NewNotification\)/) next
          print FILENAME ":" start
        }
      ' \
    | sed "s|$ROOT/||"
)"

if [ -z "$RESULT" ]; then
  echo "OK: userId 스코프 없는 Repository 메서드 0건"
  exit 0
fi

COUNT="$(printf '%s\n' "$RESULT" | wc -l | tr -d ' ')"
echo "잔여 ${COUNT}건 — 아래 메서드가 userId를 받지 않는다:"
printf '%s\n' "$RESULT"
exit 1
