package com.knowave.cashboard.domains.auth.policy

import com.knowave.cashboard.domains.auth.exception.UnsupportedSocialLoginException
import com.knowave.cashboard.domains.auth.social.ClientPlatform.ANDROID
import com.knowave.cashboard.domains.auth.social.ClientPlatform.IOS
import com.knowave.cashboard.domains.auth.social.SocialProvider
import com.knowave.cashboard.domains.auth.social.SocialProvider.APPLE
import com.knowave.cashboard.domains.auth.social.SocialProvider.GOOGLE
import com.knowave.cashboard.domains.auth.social.SocialProvider.KAKAO
import com.knowave.cashboard.domains.auth.social.SocialProvider.NAVER
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class SocialLoginPolicyTest {
	private val policy = SocialLoginPolicy()

	@ParameterizedTest
	@EnumSource(SocialProvider::class)
	fun `iOS는 4종 Provider를 모두 허용한다`(provider: SocialProvider) {
		assertThatCode { policy.validate(IOS, provider) }.doesNotThrowAnyException()
	}

	@Test
	fun `Android는 Kakao Naver Google을 허용한다`() {
		assertThatCode { policy.validate(ANDROID, KAKAO) }.doesNotThrowAnyException()
		assertThatCode { policy.validate(ANDROID, NAVER) }.doesNotThrowAnyException()
		assertThatCode { policy.validate(ANDROID, GOOGLE) }.doesNotThrowAnyException()
	}

	@Test
	fun `Android와 Apple 조합은 거부한다`() {
		assertThatThrownBy { policy.validate(ANDROID, APPLE) }
			.isInstanceOf(UnsupportedSocialLoginException::class.java)
	}
}
