package com.knowave.cashboard.domains.user.service.dto

import com.knowave.cashboard.domains.user.entity.User

data class GetOrCreateUserCommand(
	val providerName: String,
	val providerId: String,
	val email: String?,
	val providerNickname: String?,
	val profileImageUrl: String?,
	val timezone: String,
) {
	fun toEntity(nickname: String): User = User(
		provider = providerName,
		providerId = providerId,
		email = email,
		nickname = nickname,
		profileImageUrl = profileImageUrl,
		timezone = timezone,
	)
}
