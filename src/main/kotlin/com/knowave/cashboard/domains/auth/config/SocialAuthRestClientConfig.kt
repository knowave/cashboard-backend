package com.knowave.cashboard.domains.auth.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class SocialAuthRestClientConfig {
	// ponytail: 로깅 인터셉터를 두지 않는다. 이 RestClient가 보내는 모든 요청 body에는
	// client_secret(3종) 또는 서명된 JWT(Apple)가 실리므로, 인터셉터를 나중에 추가하더라도
	// 이 빈에는 body를 남기는 로깅을 절대 걸면 안 된다.
	@Bean
	fun socialAuthRestClient(): RestClient = RestClient.create()
}
