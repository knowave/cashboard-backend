package com.knowave.cashboard.domains.auth.policy

import com.knowave.cashboard.domains.auth.exception.UnsupportedSocialLoginException
import com.knowave.cashboard.domains.auth.social.ClientPlatform
import com.knowave.cashboard.domains.auth.social.ClientPlatform.ANDROID
import com.knowave.cashboard.domains.auth.social.ClientPlatform.IOS
import com.knowave.cashboard.domains.auth.social.SocialProvider
import com.knowave.cashboard.domains.auth.social.SocialProvider.APPLE
import com.knowave.cashboard.domains.auth.social.SocialProvider.GOOGLE
import com.knowave.cashboard.domains.auth.social.SocialProvider.KAKAO
import com.knowave.cashboard.domains.auth.social.SocialProvider.NAVER
import org.springframework.stereotype.Component

/**
 * (platform, provider) 조합 허용 여부. ANDROID+APPLE은 Provider HTTP 호출 전에 여기서 거부한다.
 * 향후 WEB 등 platform이 늘어나도 이 맵만 바꾸면 된다.
 */
@Component
class SocialLoginPolicy {
	private val allowedProvidersByPlatform: Map<ClientPlatform, Set<SocialProvider>> = mapOf(
		IOS to setOf(APPLE, KAKAO, NAVER, GOOGLE),
		ANDROID to setOf(KAKAO, NAVER, GOOGLE),
	)

	fun validate(platform: ClientPlatform, provider: SocialProvider) {
		if (provider !in allowedProvidersByPlatform.getValue(platform)) {
			throw UnsupportedSocialLoginException(platform, provider)
		}
	}
}
