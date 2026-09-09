package com.knowave.cashboard.domains.notification.job

import java.util.UUID

/**
 * Stage 2(인증)가 들어오기 전까지 쓰는 임시 사용자 식별자다.
 *
 * ponytail: Stage 3에서 Repository·Service 전체를 `userId` 스코프로 바꿨지만
 * `@AuthenticationPrincipal`을 제공할 `SecurityConfig`가 아직 없다. Controller와
 * 스케줄러 Job 양쪽에 값을 하나 넣어야 컴파일과 기존 동작이 유지된다.
 *
 * 상수를 두 곳에 따로 두면 값이 어긋나 이벤트 기반 알림이 API에서 안 보이는 식으로
 * 조용히 깨진다(실제로 한 번 발생했다). 그래서 단일 출처로 고정한다.
 *
 * Stage 2가 Controller를 `@AuthenticationPrincipal`로, Stage 4가 스케줄러를
 * 사용자 순회로 바꾸면 이 파일은 삭제된다.
 * `scripts/check-user-scope.sh`와 계획 Verification 6b가 잔여를 검출한다.
 */
val PLACEHOLDER_USER_ID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000000")
