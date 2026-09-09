package com.knowave.cashboard.domains.user.service

import com.knowave.cashboard.common.exception.NicknameGenerationFailedException
import com.knowave.cashboard.common.exception.UserNotPersistedException
import com.knowave.cashboard.domains.user.repository.UserRepository
import com.knowave.cashboard.domains.user.service.dto.GetOrCreateUserCommand
import com.knowave.cashboard.domains.user.service.dto.UserResult
import com.knowave.cashboard.domains.user.service.dto.toResult
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserServiceImpl(
	private val userRepository: UserRepository,
) : UserService {
	@Transactional
	override fun getOrCreate(command: GetOrCreateUserCommand): UserResult {
		userRepository.findByProviderAndProviderId(command.providerName, command.providerId)
			?.let { return it.toResult() }

		// resolveNickname()의 existsByNickname 확인은 최선의 사전 확인일 뿐이고,
		// 확인과 INSERT 사이에 다른 요청이 같은 nickname을 먼저 차지하는 경쟁이 있을 수 있다.
		// 그 경우 nickname UNIQUE 위반으로 INSERT가 실패하므로, 새 후보로 재시도한다.
		repeat(NICKNAME_CONFLICT_RETRY_LIMIT) {
			val newUser = command.toEntity(resolveNickname(command.providerNickname))

			try {
				userRepository.insertIfAbsent(newUser)
			} catch (exception: DataIntegrityViolationException) {
				// 제약 이름이나 예외 메시지 문자열에 기대지 않고, 문제였을 수 있는 사실을 직접
				// 재조회해 판정한다: 방금 쓰려던 nickname이 이미 점유돼 있으면 nickname 충돌이므로
				// 새 후보로 재시도하고, 아니면(예: email UNIQUE 위반) 그대로 노출한다.
				if (userRepository.existsByNickname(newUser.nickname)) return@repeat
				throw exception
			}

			// (provider, provider_id) 충돌은 insertIfAbsent 내부에서 DO NOTHING으로 흡수되므로,
			// 방금 생성한 행이든 동시 요청이 먼저 만든 기존 행이든 재조회로 동일하게 가져온다.
			return userRepository.findByProviderAndProviderId(command.providerName, command.providerId)
				?.toResult()
				?: throw UserNotPersistedException(command.providerName, command.providerId)
		}
		throw NicknameGenerationFailedException(command.providerNickname)
	}

	/**
	 * providerNickname(Provider가 준 nickname, 없으면 null)을 바탕으로 아직 쓰이지 않은 nickname을 만든다.
	 * providerNickname이 null이면 FALLBACK_NICKNAME에서 출발한다. 이미 점유된 값이면 짧은 판별자를
	 * 붙여 재시도하고, 그래도 겹치면 판별자 길이를 늘린다.
	 */
	private fun resolveNickname(providerNickname: String?): String {
		val baseNickname = providerNickname?.trim()?.takeIf { it.isNotEmpty() }?.take(MAX_NICKNAME_LENGTH) ?: FALLBACK_NICKNAME
		if (!userRepository.existsByNickname(baseNickname)) return baseNickname

		repeat(SHORT_DISCRIMINATOR_RETRY_LIMIT) {
			val suffixedNickname = appendDiscriminator(baseNickname, SHORT_DISCRIMINATOR_LENGTH)
			if (!userRepository.existsByNickname(suffixedNickname)) return suffixedNickname
		}
		repeat(LONG_DISCRIMINATOR_RETRY_LIMIT) {
			val suffixedNickname = appendDiscriminator(baseNickname, LONG_DISCRIMINATOR_LENGTH)
			if (!userRepository.existsByNickname(suffixedNickname)) return suffixedNickname
		}
		// exists-check 기반 판정은 TOCTOU 경쟁을 완전히 없애지 못한다.
		// 최종 방어선은 getOrCreate가 실제 INSERT의 UNIQUE(nickname) 위반을 잡아 이 메서드를 다시 호출하는
		// 재시도이므로, 여기서는 마지막 후보를 그냥 반환한다.
		return appendDiscriminator(baseNickname, LONG_DISCRIMINATOR_LENGTH)
	}

	private fun appendDiscriminator(baseNickname: String, discriminatorLength: Int): String {
		val discriminator = UUID.randomUUID().toString().replace("-", "").take(discriminatorLength)
		val truncatedBaseNickname = baseNickname.take(MAX_NICKNAME_LENGTH - discriminator.length - 1)
		return "$truncatedBaseNickname-$discriminator"
	}

	companion object {
		private const val NICKNAME_CONFLICT_RETRY_LIMIT = 5
		private const val FALLBACK_NICKNAME = "user"
		private const val MAX_NICKNAME_LENGTH = 255
		private const val SHORT_DISCRIMINATOR_LENGTH = 4
		private const val LONG_DISCRIMINATOR_LENGTH = 8
		private const val SHORT_DISCRIMINATOR_RETRY_LIMIT = 5
		private const val LONG_DISCRIMINATOR_RETRY_LIMIT = 3
	}
}
