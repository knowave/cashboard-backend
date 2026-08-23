package com.knowave.cashboard.domains.assetgoal.entity

import com.knowave.cashboard.common.exception.InvalidSavingPeriodException
import com.knowave.cashboard.domains.assetgoal.service.dto.UpdateSavingRecordCommand
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class SavingRecordTest {
	@Test
	fun `SavingPeriod는 3 6 12 24 36개월만 허용한다`() {
		assertThat(SavingPeriod.from(3)).isEqualTo(SavingPeriod.THREE)
		assertThat(SavingPeriod.from(36)).isEqualTo(SavingPeriod.THIRTY_SIX)
		assertThatThrownBy { SavingPeriod.from(4) }
			.isInstanceOf(InvalidSavingPeriodException::class.java)
	}

	@Test
	fun `applyUpdate는 변경된 월별 저축 실적을 반영한다`() {
		val savingRecord = SavingRecord(
			targetMonth = "2026-07",
			amount = 500_000L,
			memo = "기존",
		)

		SavingRecord.applyUpdate(
			savingRecord = savingRecord,
			command = UpdateSavingRecordCommand(
				targetMonth = "2026-08",
				amount = 700_000L,
				memo = "수정",
			),
		)

		assertThat(savingRecord.targetMonth).isEqualTo("2026-08")
		assertThat(savingRecord.amount).isEqualTo(700_000L)
		assertThat(savingRecord.memo).isEqualTo("수정")
	}
}
