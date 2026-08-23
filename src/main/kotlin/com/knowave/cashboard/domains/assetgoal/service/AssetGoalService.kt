package com.knowave.cashboard.domains.assetgoal.service

import com.knowave.cashboard.domains.assetgoal.service.dto.AssetGoalDetailResult
import com.knowave.cashboard.domains.assetgoal.service.dto.AssetGoalSimulationCommand
import com.knowave.cashboard.domains.assetgoal.service.dto.AssetGoalSimulationResult
import com.knowave.cashboard.domains.assetgoal.service.dto.AssetGoalSummaryResult
import com.knowave.cashboard.domains.assetgoal.service.dto.CreateAssetGoalCommand
import com.knowave.cashboard.domains.assetgoal.service.dto.CreateSavingRecordCommand
import com.knowave.cashboard.domains.assetgoal.service.dto.SavingRecordResult
import com.knowave.cashboard.domains.assetgoal.service.dto.UpdateAssetGoalCommand
import com.knowave.cashboard.domains.assetgoal.service.dto.UpdateSavingRecordCommand
import java.util.UUID

interface AssetGoalService {
	fun createAssetGoal(command: CreateAssetGoalCommand): AssetGoalDetailResult
	fun getAssetGoalSummaries(savingPeriodMonths: Int): List<AssetGoalSummaryResult>
	fun getAssetGoalDetail(assetGoalId: UUID, savingPeriodMonths: Int): AssetGoalDetailResult
	fun updateAssetGoal(assetGoalId: UUID, command: UpdateAssetGoalCommand): AssetGoalDetailResult
	fun deleteAssetGoal(assetGoalId: UUID): Boolean
	fun simulateAssetGoal(assetGoalId: UUID, command: AssetGoalSimulationCommand): AssetGoalSimulationResult
	fun recordMonthlySaving(command: CreateSavingRecordCommand): SavingRecordResult
	fun getMonthlySavingRecords(periodMonths: Int): List<SavingRecordResult>
	fun getMonthlySavingRecord(targetMonth: String): SavingRecordResult
	fun updateMonthlySaving(id: UUID, command: UpdateSavingRecordCommand): SavingRecordResult
	fun deleteMonthlySaving(id: UUID): Boolean
}
