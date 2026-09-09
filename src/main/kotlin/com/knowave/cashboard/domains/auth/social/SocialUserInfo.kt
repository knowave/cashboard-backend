package com.knowave.cashboard.domains.auth.social

data class SocialUserInfo(
	val provider: SocialProvider,
	val providerId: String,
	val email: String?,
	val nickname: String?,
	val profileImageUrl: String?,
)
