package com.knowave.cashboard.domains.assetgoal.controller

import com.knowave.cashboard.common.response.ApiResponse
import com.knowave.cashboard.common.response.success
import com.knowave.cashboard.common.security.AuthenticatedUser
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
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
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

@Validated
@RestController
class AssetGoalController(
	private val assetGoalService: AssetGoalService,
) {
	@PostMapping("/asset-goals")
	@ResponseStatus(HttpStatus.CREATED)
	fun createAssetGoal(@AuthenticationPrincipal user: AuthenticatedUser, @Valid @RequestBody request: AssetGoalCreateRequest): ApiResponse<AssetGoalDetailResponse> =
		success(assetGoalService.createAssetGoal(user.userId, request.toCommand()).toResponse())

	@GetMapping("/asset-goals")
	fun getAssetGoalSummaries(
		@AuthenticationPrincipal user: AuthenticatedUser,
		@RequestParam(defaultValue = "3") savingPeriodMonths: Int,
	): ApiResponse<List<AssetGoalSummaryResponse>> =
		success(assetGoalService.getAssetGoalSummaries(user.userId, savingPeriodMonths).map { it.toResponse() })

	@GetMapping("/asset-goals/{assetGoalId}")
	fun getAssetGoalDetail(
		@AuthenticationPrincipal user: AuthenticatedUser,
		@PathVariable assetGoalId: UUID,
		@RequestParam(defaultValue = "3") savingPeriodMonths: Int,
	): ApiResponse<AssetGoalDetailResponse> =
		success(assetGoalService.getAssetGoalDetail(user.userId, assetGoalId, savingPeriodMonths).toResponse())

	@PatchMapping("/asset-goals/{assetGoalId}")
	fun updateAssetGoal(
		@AuthenticationPrincipal user: AuthenticatedUser,
		@PathVariable assetGoalId: UUID,
		@Valid @RequestBody request: AssetGoalUpdateRequest,
	): ApiResponse<AssetGoalDetailResponse> =
		success(assetGoalService.updateAssetGoal(user.userId, assetGoalId, request.toCommand()).toResponse())

	@DeleteMapping("/asset-goals/{assetGoalId}")
	fun deleteAssetGoal(@AuthenticationPrincipal user: AuthenticatedUser, @PathVariable assetGoalId: UUID): ApiResponse<Boolean> =
		success(assetGoalService.deleteAssetGoal(user.userId, assetGoalId))

	@PostMapping("/asset-goals/{assetGoalId}/simulations")
	fun simulateAssetGoal(
		@AuthenticationPrincipal user: AuthenticatedUser,
		@PathVariable assetGoalId: UUID,
		@Valid @RequestBody request: AssetGoalSimulationRequest,
	): ApiResponse<AssetGoalSimulationResponse> =
		success(assetGoalService.simulateAssetGoal(user.userId, assetGoalId, request.toCommand()).toResponse())

	@PostMapping("/saving-records")
	@ResponseStatus(HttpStatus.CREATED)
	fun recordMonthlySaving(@AuthenticationPrincipal user: AuthenticatedUser, @Valid @RequestBody request: SavingRecordRequest): ApiResponse<SavingRecordResponse> =
		success(assetGoalService.recordMonthlySaving(user.userId, request.toCommand()).toResponse())

	@GetMapping("/saving-records")
	fun getMonthlySavingRecords(
		@AuthenticationPrincipal user: AuthenticatedUser,
		@RequestParam(defaultValue = "3") periodMonths: Int,
	): ApiResponse<List<SavingRecordResponse>> =
		success(assetGoalService.getMonthlySavingRecords(user.userId, periodMonths).map { it.toResponse() })

	@GetMapping("/saving-records/{targetMonth}")
	fun getMonthlySavingRecord(
		@AuthenticationPrincipal user: AuthenticatedUser,
		@PathVariable
		@Pattern(regexp = "\\d{4}-\\d{2}", message = "targetMonth must be yyyy-MM.")
		targetMonth: String,
	): ApiResponse<SavingRecordResponse> =
		success(assetGoalService.getMonthlySavingRecord(user.userId, targetMonth).toResponse())

	@PatchMapping("/saving-records/{id}")
	fun updateMonthlySaving(
		@AuthenticationPrincipal user: AuthenticatedUser,
		@PathVariable id: UUID,
		@Valid @RequestBody request: SavingRecordUpdateRequest,
	): ApiResponse<SavingRecordResponse> =
		success(assetGoalService.updateMonthlySaving(user.userId, id, request.toCommand()).toResponse())

	@DeleteMapping("/saving-records/{id}")
	fun deleteMonthlySaving(@AuthenticationPrincipal user: AuthenticatedUser, @PathVariable id: UUID): ApiResponse<Boolean> =
		success(assetGoalService.deleteMonthlySaving(user.userId, id))

}
