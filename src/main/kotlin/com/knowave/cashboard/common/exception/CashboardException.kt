package com.knowave.cashboard.common.exception

import org.springframework.http.HttpStatus

open class CashboardException(
	val errorCode: String,
	override val message: String,
	val status: HttpStatus = HttpStatus.BAD_REQUEST,
) : RuntimeException(message)

class NotFoundException(resourceName: String, id: Any) : CashboardException(
	errorCode = "NOT_FOUND",
	message = "$resourceName not found. id=$id",
	status = HttpStatus.NOT_FOUND,
)

class InvalidEnumValueException(enumName: String, value: String) : CashboardException(
	errorCode = "INVALID_ENUM_VALUE",
	message = "Invalid $enumName value: $value",
	status = HttpStatus.BAD_REQUEST,
)

/**
 * 영속화된 엔티티를 읽었는데 id가 null인 경우. @GeneratedValue id는 persist 후 채워지므로
 * 이 상태는 매핑 경로가 잘못된 것이지 클라이언트 잘못이 아니다.
 */
class PersistedEntityIdMissingException(entityName: String) : CashboardException(
	errorCode = "PERSISTED_ENTITY_ID_MISSING",
	message = "Persisted $entityName has no id.",
	status = HttpStatus.INTERNAL_SERVER_ERROR,
)

/**
 * 필수 설정 프로퍼티가 없거나 요구 조건을 만족하지 못해 부팅을 중단시킬 때 쓴다.
 *
 * ponytail: status는 이 예외가 HTTP 응답으로 나갈 때만 의미가 있다. 부팅 경로에서는 Spring이
 * BeanCreationException으로 감싸 컨텍스트 초기화를 중단시키므로 status는 쓰이지 않는다.
 * 그래도 프로젝트의 단일 예외 계층을 벗어나지 않기 위해 CashboardException을 상속한다.
 */
class MissingRequiredPropertyException(propertyKey: String, requirement: String) : CashboardException(
	errorCode = "MISSING_REQUIRED_PROPERTY",
	message = "$propertyKey $requirement",
	status = HttpStatus.INTERNAL_SERVER_ERROR,
)
