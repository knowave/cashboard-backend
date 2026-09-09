package com.knowave.cashboard.common.security

import java.util.UUID
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.oauth2.jwt.Jwt

// 리소스 서버가 기본으로 principal에 Jwt를 넣는다. Controller가 AuthenticatedUser를
// @AuthenticationPrincipal로 바로 받도록 principal 타입을 여기서 치환한다.
// Role 개념이 없으므로 authorities는 항상 비워 둔다.
class JwtPrincipalConverter : Converter<Jwt, AbstractAuthenticationToken> {
	override fun convert(source: Jwt): AbstractAuthenticationToken {
		val userId = UUID.fromString(source.subject)
		return UsernamePasswordAuthenticationToken(AuthenticatedUser(userId), source, emptyList())
	}
}
