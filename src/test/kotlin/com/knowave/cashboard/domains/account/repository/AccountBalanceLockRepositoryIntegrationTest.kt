package com.knowave.cashboard.domains.account.repository

import com.knowave.cashboard.support.PostgreSqlIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.support.TransactionTemplate
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class AccountBalanceLockRepositoryIntegrationTest : PostgreSqlIntegrationTest() {
	@Autowired lateinit var lockRepository: AccountBalanceLockRepository
	@Autowired lateinit var transactionTemplate: TransactionTemplate

	@Test
	fun `두 번째 트랜잭션은 첫 번째 트랜잭션이 끝나기 전에는 총자산 잠금을 얻지 못한다`() {
		val firstLockAcquired = CountDownLatch(1)
		val releaseFirstTransaction = CountDownLatch(1)
		val secondTransactionEntered = CountDownLatch(1)
		val secondLockAcquired = CountDownLatch(1)
		val executor = Executors.newFixedThreadPool(2)

		val first = executor.submit {
			transactionTemplate.executeWithoutResult {
				lockRepository.acquireTotalAssetLock()
				firstLockAcquired.countDown()
				check(releaseFirstTransaction.await(10, TimeUnit.SECONDS))
			}
		}
		try {
			check(firstLockAcquired.await(10, TimeUnit.SECONDS))
			val second = executor.submit {
				transactionTemplate.executeWithoutResult {
					secondTransactionEntered.countDown()
					lockRepository.acquireTotalAssetLock()
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
}
