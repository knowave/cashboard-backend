package com.knowave.cashboard.domains.auth.social

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Google/Kakao/Naver/Apple 4종 모두 code->token 교환 응답에서 `access_token`을 준다.
 * Apple만 `id_token`을 추가로 준다. 공통 필드만 여기서 흡수한다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
internal data class ProviderTokenResponse(
	@JsonProperty("access_token") val accessToken: String,
	@JsonProperty("id_token") val idToken: String? = null,
)
