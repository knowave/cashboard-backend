package com.knowave.cashboard.domains.user.service

import com.knowave.cashboard.domains.user.service.dto.GetOrCreateUserCommand
import com.knowave.cashboard.domains.user.service.dto.UserResult

interface UserService {
	/**
	 * 소셜 로그인 결과로 (providerName, providerId)에 대응하는 User를 조회하거나 없으면 생성한다.
	 * nickname 충돌(동명이인) 시 판별자를 붙여 재시도하며, (providerName, providerId) 동시 생성 경쟁은
	 * UserRepository.insertIfAbsent가 흡수한다.
	 */
	fun getOrCreate(command: GetOrCreateUserCommand): UserResult
}
