package com.knowave.cashboard.domains.assetgoal.repository

import com.knowave.cashboard.domains.assetgoal.entity.SavingRecord
import com.knowave.cashboard.support.PostgreSqlIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate

class SavingRecordRepositoryIntegrationTest : PostgreSqlIntegrationTest() {
	@Autowired lateinit var savingRecordRepository: SavingRecordRepository
	@Autowired lateinit var jdbcTemplate: JdbcTemplate

	@BeforeEach
	fun isolateDatabase() {
		jdbcTemplate.execute("TRUNCATE TABLE saving_records CASCADE")
	}

	// AC-20d: MonthlyBudget과 같은 성질의 버그. 최종 상태(V8 이후 복합 UNIQUE(user_id, target_month))
	// 기준의 의도를 코드로 남긴다.
	@Disabled(
		"V8__enforce_user_ownership.sql(Stage 3.5) 적용 후 활성화. " +
			"현재는 글로벌 UNIQUE(target_month)가 살아 있어 두 사용자가 같은 월을 가질 수 없다.",
	)
	@Test
	fun `사용자 A와 B는 같은 target_month로 각각 저축 기록을 생성할 수 있다`() {
		val userA = persistUser()
		val userB = persistUser()
		savingRecordRepository.save(SavingRecord(userA.id, "2026-09", 500_000L, null))

		assertThat(savingRecordRepository.existsByTargetMonthAndUserId("2026-09", userB.id)).isFalse()

		savingRecordRepository.save(SavingRecord(userB.id, "2026-09", 300_000L, null))
	}
}
