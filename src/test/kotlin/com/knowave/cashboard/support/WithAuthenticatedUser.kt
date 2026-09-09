package com.knowave.cashboard.support

import com.knowave.cashboard.common.security.AuthenticatedUser
import java.util.UUID
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithSecurityContext
import org.springframework.security.test.context.support.WithSecurityContextFactory

/**
 * `@AuthenticationPrincipal AuthenticatedUser`를 받는 Controller의 슬라이스 테스트용 애노테이션이다.
 *
 * ponytail: Spring Security Test가 제공하는 확장점(`@WithSecurityContext`)을 그대로 쓴다.
 * `@WithMockUser`는 principal이 Spring의 `User` 타입이라 `AuthenticatedUser`로 해석되지 않는다.
 *
 * 기본 `userId`는 Controller 테스트들이 이미 쓰는 `tempUserId`(nil UUID)와 같은 값이다 —
 * 기존 stub의 `given(...)`/`verify(...)` 인자와 일치시키기 위한 것이다.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@WithSecurityContext(factory = AuthenticatedUserSecurityContextFactory::class)
annotation class WithAuthenticatedUser(
	val userId: String = "00000000-0000-0000-0000-000000000000",
)

class AuthenticatedUserSecurityContextFactory : WithSecurityContextFactory<WithAuthenticatedUser> {
	override fun createSecurityContext(annotation: WithAuthenticatedUser): SecurityContext =
		SecurityContextHolder.createEmptyContext().apply {
			authentication = UsernamePasswordAuthenticationToken(
				AuthenticatedUser(UUID.fromString(annotation.userId)),
				null,
				emptyList(),
			)
		}
}
