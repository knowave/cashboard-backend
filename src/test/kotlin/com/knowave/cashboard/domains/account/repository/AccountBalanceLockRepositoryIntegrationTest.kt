package com.knowave.cashboard.domains.account.repository

import com.knowave.cashboard.support.PostgreSqlIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.support.TransactionTemplate
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class AccountBalanceLockRepositoryIntegrationTest : PostgreSqlIntegrationTest() {
	@Autowired lateinit var lockRepository: AccountBalanceLockRepository
	@Autowired lateinit var transactionTemplate: TransactionTemplate

	@Test
	fun `두 번째 트랜잭션은 첫 번째 트랜잭션이 끝나기 전에는 총자산 잠금을 얻지 못한다`() {
		// ponytail: 컴파일 통과만을 위한 최소 수정. userId 인자별 격리 검증은 T7 담당.
		val userId = UUID.randomUUID()
		val firstLockAcquired = CountDownLatch(1)
		val releaseFirstTransaction = CountDownLatch(1)
		val secondTransactionEntered = CountDownLatch(1)
		val secondLockAcquired = CountDownLatch(1)
		val executor = Executors.newFixedThreadPool(2)

		val first = executor.submit {
			transactionTemplate.executeWithoutResult {
				lockRepository.acquireTotalAssetLock(userId)
				firstLockAcquired.countDown()
				check(releaseFirstTransaction.await(10, TimeUnit.SECONDS))
			}
		}
		try {
			check(firstLockAcquired.await(10, TimeUnit.SECONDS))
			val second = executor.submit {
				transactionTemplate.executeWithoutResult {
					secondTransactionEntered.countDown()
					lockRepository.acquireTotalAssetLock(userId)
					secondLockAcquired.countDown()
				}
			}

			check(secondTransactionEntered.await(10, TimeUnit.SECONDS))
			assertThat(secondLockAcquired.await(1, TimeUnit.SECONDS)).isFalse()

			releaseFirstTransaction.countDown()
			first.get(10, TimeUnit.SECONDS)
			second.get(10, TimeUnit.SECONDS)
		} finally {
			releaseFirstTransaction.countDown()
			executor.shutdownNow()
			check(executor.awaitTermination(10, TimeUnit.SECONDS))
		}
	}

	@Test
	fun `서로 다른 userId의 총자산 갱신은 서로를 차단하지 않는다`() {
		// AC-19: hashtextextended(userId, seed) 전환이 사용자별로 실제 다른 advisory lock을 만드는지 검증한다.
		val firstUserId = UUID.randomUUID()
		val secondUserId = UUID.randomUUID()
		val firstLockAcquired = CountDownLatch(1)
		val releaseFirstTransaction = CountDownLatch(1)
		val secondLockAcquired = CountDownLatch(1)
		val executor = Executors.newFixedThreadPool(2)

		val first = executor.submit {
			transactionTemplate.executeWithoutResult {
				lockRepository.acquireTotalAssetLock(firstUserId)
				firstLockAcquired.countDown()
				check(releaseFirstTransaction.await(10, TimeUnit.SECONDS))
			}
		}
		try {
			check(firstLockAcquired.await(10, TimeUnit.SECONDS))
			val second = executor.submit {
				transactionTemplate.executeWithoutResult {
					lockRepository.acquireTotalAssetLock(secondUserId)
					secondLockAcquired.countDown()
				}
			}

			assertThat(secondLockAcquired.await(5, TimeUnit.SECONDS)).isTrue()

			releaseFirstTransaction.countDown()
			first.get(10, TimeUnit.SECONDS)
			second.get(10, TimeUnit.SECONDS)
		} finally {
			releaseFirstTransaction.countDown()
			executor.shutdownNow()
			check(executor.awaitTermination(10, TimeUnit.SECONDS))
		}
	}
}
