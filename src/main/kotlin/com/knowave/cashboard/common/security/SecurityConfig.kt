package com.knowave.cashboard.common.security

import com.knowave.cashboard.common.exception.RestAccessDeniedHandler
import com.knowave.cashboard.common.exception.RestAuthenticationEntryPoint
import com.knowave.cashboard.domains.auth.security.RateLimitFilter
import javax.crypto.spec.SecretKeySpec
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtIssuerValidator
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
class SecurityConfig(
	@Value("\${auth.jwt.secret}") private val jwtSecret: String,
	@Value("\${auth.jwt.issuer}") private val jwtIssuer: String,
	@Value("\${cors.allowed-origin}") private val allowedOrigin: String,
	private val environment: Environment,
	private val rateLimitFilter: RateLimitFilter,
	private val authenticationEntryPoint: RestAuthenticationEntryPoint,
	private val accessDeniedHandler: RestAccessDeniedHandler,
) {
	@Bean
	fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
		// swagger 공개는 개발 프로필 한정이다 (전 프로필 공개는 드리프트).
		val devProfileActive = environment.acceptsProfiles(Profiles.of("dev"))
		http.addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter::class.java)
		http {
			csrf { disable() }
			cors { }
			sessionManagement { sessionCreationPolicy = SessionCreationPolicy.STATELESS }
			authorizeHttpRequests {
				authorize(HttpMethod.POST, "/auth/social/login", permitAll)
				authorize(HttpMethod.POST, "/auth/refresh", permitAll)
				// logout도 공개다. Refresh Token 자체가 자격 증명이고, authenticated()로 막으면
				// Access Token이 만료된 사용자(가장 흔한 로그아웃 시점)가 로그아웃을 못 한다.
				// 탈취된 Refresh로 남을 로그아웃시키는 것은 그 토큰으로 refresh하는 것보다 해가 적다.
				authorize(HttpMethod.POST, "/auth/logout", permitAll)

				if (devProfileActive) {
					authorize("/swagger-ui/**", permitAll)
					authorize("/api-docs/**", permitAll)
				}

				authorize(anyRequest, authenticated)
			}
			oauth2ResourceServer {
				jwt {
					jwtDecoder = jwtDecoder()
					jwtAuthenticationConverter = JwtPrincipalConverter()
				}
			}
			exceptionHandling {
				authenticationEntryPoint = this@SecurityConfig.authenticationEntryPoint
				accessDeniedHandler = this@SecurityConfig.accessDeniedHandler
			}
		}
		return http.build()
	}

	// 컨텍스트에는 이 decoder 하나만 빈으로 존재해야 한다. Apple JWKS decoder는
	// AppleIdentityTokenVerifier의 private 필드로만 존재한다 — 빈이 2개면 주입이 모호해져 부팅이 죽는다.
	@Bean
	fun jwtDecoder(): JwtDecoder {
		val key = SecretKeySpec(jwtSecret.toByteArray(), "HmacSHA256")
		val decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build()
		// withSecretKey는 기본적으로 iss/aud를 검사하지 않는다 — 같은 시크릿으로 서명된 임의의
		// HS256 토큰이 통과하므로 issuer 검증을 명시적으로 건다.
		decoder.setJwtValidator(JwtIssuerValidator(jwtIssuer))
		return decoder
	}

	@Bean
	fun corsConfigurationSource(): CorsConfigurationSource {
		val configuration = CorsConfiguration().apply {
			allowedOrigins = listOf(allowedOrigin)
			allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
			allowedHeaders = listOf("*")
			allowCredentials = true
		}
		return UrlBasedCorsConfigurationSource().apply {
			registerCorsConfiguration("/**", configuration)
		}
	}
}
