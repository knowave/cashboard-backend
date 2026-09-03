# 프로젝트 작업 지침

## 문서 언어

- 프로젝트에서 새로 작성하거나 수정하는 문서는 반드시 한국어로 작성한다.
- 코드, API 경로, 클래스·메서드·필드명, 파일 경로, 명령어, 오류 코드처럼 정확한 표기가 필요한 기술 식별자는 원문을 유지한다.
- 외부 도구가 요구하는 고정 문구가 있으면 해당 문구만 예외로 두고, 나머지 설명은 한국어로 작성한다.

## 작업 권한

- 파일 읽기·쓰기, 일반 명령어 실행, Bash 사용은 사용자에게 별도 허락을 묻지 않고 진행한다.
- `git add`, `git commit`, `git push`를 실행하기 전에는 반드시 사용자에게 허락을 받는다.
- Codex 실행 환경이나 샌드박스가 권한 승인을 강제하는 작업은 가능한 안전한 대안을 사용한다. 대안이 없으면 해당 작업을 실행하거나 중간 승인을 요청하지 않고 최종 보고에 제약 사항을 기록한다.

## 작업 중 소통

- 작업 중에는 사용자에게 질문, 선택 요청, 승인 요청 또는 중간 진행 보고를 하지 않는다.
- 발견 가능한 프로젝트 정보와 합리적인 가정을 바탕으로 자율적으로 작업하고, 완료 결과와 검증 내용만 최종 보고한다.
- 사용자 승인이 반드시 필요한 작업은 실행하지 않는다. 특히 별도 허락이 없는 `git add`, `git commit`, `git push`는 생략하고 변경 내용을 unstaged·uncommitted 상태로 유지한다.
- 사용자 결정 없이는 결과가 크게 달라져 작업을 진행할 수 없는 경우에도 질문하지 않고, 안전하게 완료할 수 있는 범위까지만 처리한 뒤 남은 제약을 최종 보고한다.

## 모델 운영

- Brainstorming은 `Sol High`로 수행한다.
- Writing Plan은 `Sol High`로 수행한다.
- 구현은 `Terra Medium`의 Implementer agent 여러 개(`Implementer A`, `Implementer B`, `Implementer C`)로 분리해 수행한다.
- Integration Review는 변경 위험도와 복잡도에 따라 `Terra Medium` 또는 `Terra High`로 수행한다.
- Final Review는 고위험 변경에만 `Sol High`로 수행한다.
- 기본 작업 흐름은 `Sol High Brainstorming` → `Sol High Writing Plan` → `Terra Medium Implementers` → `Terra Medium/High Integration Review` 순서로 진행하고, 고위험 변경이면 마지막에 `Sol High Final Review`를 추가한다.

## 작업 위치와 Git 상태

- 별도 Git worktree를 생성하거나 사용하지 않고, 프로젝트의 기본 작업공간에서 현재 활성화된 브랜치에 직접 작업한다.
- 사용자가 명시적으로 요청한 경우에만 Git worktree를 사용한다.
- 사용자가 변경 내용을 직접 확인할 수 있도록, 별도 요청 전까지 변경 파일을 unstaged·uncommitted 상태로 유지한다.

## Enum 영속화

- 변경 가능성이 있는 enum은 DB-native enum이나 JPA `@Enumerated`로 영속화하지 않는다.
- DB 컬럼과 Entity 필드는 각각 `VARCHAR`, `String`으로 정의한다.
- 문자열과 enum 사이의 변환 및 유효성 검증은 서비스 계층에서 수행한다.
- 저장할 때는 `enum.name`으로 값을 정규화한다.
- enum 값 목록을 고정하는 DB `CHECK` 제약조건은 추가하지 않는다.

## Service 구조

- Service interface와 구현 클래스는 반드시 서로 다른 파일로 분리한다.
- `*Service.kt`에는 외부에 노출할 Service interface와 해당 계약에 필요한 최소 import만 둔다.
- `*ServiceImpl.kt`에는 구현 클래스와 구현에 필요한 의존성·애노테이션만 둔다.
- 하나의 파일에 Service interface와 구현 클래스를 함께 선언하지 않는다.
