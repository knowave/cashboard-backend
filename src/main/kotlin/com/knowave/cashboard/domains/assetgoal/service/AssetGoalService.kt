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
	fun createAssetGoal(userId: UUID, command: CreateAssetGoalCommand): AssetGoalDetailResult
	fun getAssetGoalSummaries(userId: UUID, savingPeriodMonths: Int): List<AssetGoalSummaryResult>
	fun getAssetGoalDetail(userId: UUID, assetGoalId: UUID, savingPeriodMonths: Int): AssetGoalDetailResult
	fun updateAssetGoal(userId: UUID, assetGoalId: UUID, command: UpdateAssetGoalCommand): AssetGoalDetailResult
	fun deleteAssetGoal(userId: UUID, assetGoalId: UUID): Boolean
	fun simulateAssetGoal(
		userId: UUID,
		assetGoalId: UUID,
		command: AssetGoalSimulationCommand,
	): AssetGoalSimulationResult
	fun recordMonthlySaving(userId: UUID, command: CreateSavingRecordCommand): SavingRecordResult
	fun getMonthlySavingRecords(userId: UUID, periodMonths: Int): List<SavingRecordResult>
	fun getMonthlySavingRecord(userId: UUID, targetMonth: String): SavingRecordResult
	fun updateMonthlySaving(userId: UUID, id: UUID, command: UpdateSavingRecordCommand): SavingRecordResult
	fun deleteMonthlySaving(userId: UUID, id: UUID): Boolean
}
