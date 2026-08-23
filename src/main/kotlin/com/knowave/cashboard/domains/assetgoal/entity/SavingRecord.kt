package com.knowave.cashboard.domains.assetgoal.entity

import com.knowave.cashboard.common.entity.BaseEntity
import com.knowave.cashboard.domains.assetgoal.service.dto.UpdateSavingRecordCommand
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "saving_records")
class SavingRecord(
	@Column(name = "target_month", nullable = false, unique = true, length = 7)
	var targetMonth: String,

	@Column(name = "amount", nullable = false)
	var amount: Long,

	@Column(name = "memo", length = 255)
	var memo: String? = null,
) : BaseEntity() {
	companion object {
		fun applyUpdate(savingRecord: SavingRecord, command: UpdateSavingRecordCommand): SavingRecord {
			if (savingRecord.targetMonth != command.targetMonth) {
				savingRecord.targetMonth = command.targetMonth
			}
			if (savingRecord.amount != command.amount) {
				savingRecord.amount = command.amount
			}
			if (savingRecord.memo != command.memo) {
				savingRecord.memo = command.memo
			}
			return savingRecord
		}
	}
}
