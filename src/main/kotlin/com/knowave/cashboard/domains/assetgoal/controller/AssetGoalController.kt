package com.knowave.cashboard.domains.assetgoal.controller

import com.knowave.cashboard.common.response.ApiResponse
import com.knowave.cashboard.common.response.success
import com.knowave.cashboard.domains.assetgoal.controller.dto.AssetGoalCreateRequest
import com.knowave.cashboard.domains.assetgoal.controller.dto.AssetGoalDetailResponse
import com.knowave.cashboard.domains.assetgoal.controller.dto.AssetGoalSimulationRequest
import com.knowave.cashboard.domains.assetgoal.controller.dto.AssetGoalSimulationResponse
import com.knowave.cashboard.domains.assetgoal.controller.dto.AssetGoalSummaryResponse
import com.knowave.cashboard.domains.assetgoal.controller.dto.AssetGoalUpdateRequest
import com.knowave.cashboard.domains.assetgoal.controller.dto.SavingRecordRequest
import com.knowave.cashboard.domains.assetgoal.controller.dto.SavingRecordResponse
import com.knowave.cashboard.domains.assetgoal.controller.dto.SavingRecordUpdateRequest
import com.knowave.cashboard.domains.assetgoal.controller.dto.toResponse
import com.knowave.cashboard.domains.assetgoal.service.AssetGoalService
import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Validated
@RestController
class AssetGoalController(
	private val assetGoalService: AssetGoalService,
) {
	@PostMapping("/asset-goals")
	@ResponseStatus(HttpStatus.CREATED)
	fun createAssetGoal(@Valid @RequestBody request: AssetGoalCreateRequest): ApiResponse<AssetGoalDetailResponse> =
		success(assetGoalService.createAssetGoal(request.toCommand()).toResponse())

	@GetMapping("/asset-goals")
	fun getAssetGoalSummaries(
		@RequestParam(defaultValue = "3") savingPeriodMonths: Int,
	): ApiResponse<List<AssetGoalSummaryResponse>> =
		success(assetGoalService.getAssetGoalSummaries(savingPeriodMonths).map { it.toResponse() })

	@GetMapping("/asset-goals/{assetGoalId}")
	fun getAssetGoalDetail(
		@PathVariable assetGoalId: UUID,
		@RequestParam(defaultValue = "3") savingPeriodMonths: Int,
	): ApiResponse<AssetGoalDetailResponse> =
		success(assetGoalService.getAssetGoalDetail(assetGoalId, savingPeriodMonths).toResponse())

	@PatchMapping("/asset-goals/{assetGoalId}")
	fun updateAssetGoal(
		@PathVariable assetGoalId: UUID,
		@Valid @RequestBody request: AssetGoalUpdateRequest,
	): ApiResponse<AssetGoalDetailResponse> =
		success(assetGoalService.updateAssetGoal(assetGoalId, request.toCommand()).toResponse())

	@DeleteMapping("/asset-goals/{assetGoalId}")
	fun deleteAssetGoal(@PathVariable assetGoalId: UUID): ApiResponse<Boolean> =
		success(assetGoalService.deleteAssetGoal(assetGoalId))

	@PostMapping("/asset-goals/{assetGoalId}/simulations")
	fun simulateAssetGoal(
		@PathVariable assetGoalId: UUID,
		@Valid @RequestBody request: AssetGoalSimulationRequest,
	): ApiResponse<AssetGoalSimulationResponse> =
		success(assetGoalService.simulateAssetGoal(assetGoalId, request.toCommand()).toResponse())

	@PostMapping("/saving-records")
	@ResponseStatus(HttpStatus.CREATED)
	fun recordMonthlySaving(@Valid @RequestBody request: SavingRecordRequest): ApiResponse<SavingRecordResponse> =
		success(assetGoalService.recordMonthlySaving(request.toCommand()).toResponse())

	@GetMapping("/saving-records")
	fun getMonthlySavingRecords(
		@RequestParam(defaultValue = "3") periodMonths: Int,
	): ApiResponse<List<SavingRecordResponse>> =
		success(assetGoalService.getMonthlySavingRecords(periodMonths).map { it.toResponse() })

	@GetMapping("/saving-records/{targetMonth}")
	fun getMonthlySavingRecord(
		@PathVariable
		@Pattern(regexp = "\\d{4}-\\d{2}", message = "targetMonth must be yyyy-MM.")
		targetMonth: String,
	): ApiResponse<SavingRecordResponse> =
		success(assetGoalService.getMonthlySavingRecord(targetMonth).toResponse())

	@PatchMapping("/saving-records/{id}")
	fun updateMonthlySaving(
		@PathVariable id: UUID,
		@Valid @RequestBody request: SavingRecordUpdateRequest,
	): ApiResponse<SavingRecordResponse> =
		success(assetGoalService.updateMonthlySaving(id, request.toCommand()).toResponse())

	@DeleteMapping("/saving-records/{id}")
	fun deleteMonthlySaving(@PathVariable id: UUID): ApiResponse<Boolean> =
		success(assetGoalService.deleteMonthlySaving(id))
}
