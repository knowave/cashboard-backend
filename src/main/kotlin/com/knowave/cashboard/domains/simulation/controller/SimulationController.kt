package com.knowave.cashboard.domains.simulation.controller

import com.knowave.cashboard.common.response.ApiResponse
import com.knowave.cashboard.common.response.success
import com.knowave.cashboard.domains.simulation.controller.dto.EarlyRepaymentSimulationRequest
import com.knowave.cashboard.domains.simulation.controller.dto.EarlyRepaymentSimulationResponse
import com.knowave.cashboard.domains.simulation.controller.dto.LoanRepaymentSimulationRequest
import com.knowave.cashboard.domains.simulation.controller.dto.LoanRepaymentSimulationResponse
import com.knowave.cashboard.domains.simulation.controller.dto.MonthlySimulationResponse
import com.knowave.cashboard.domains.simulation.controller.dto.monthlySimulationCommand
import com.knowave.cashboard.domains.simulation.controller.dto.toResponse
import com.knowave.cashboard.domains.simulation.service.SimulationFacade
import com.knowave.cashboard.domains.simulation.service.SimulationService
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Pattern
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/simulations")
class SimulationController(
	private val simulationService: SimulationService,
	private val simulationFacade: SimulationFacade,
) {
	@GetMapping("/monthly")
	fun simulateMonthly(
		@RequestParam
		@Pattern(regexp = "\\d{4}-\\d{2}", message = "from must be yyyy-MM.")
		from: String,

		@RequestParam
		@Pattern(regexp = "\\d{4}-\\d{2}", message = "to must be yyyy-MM.")
		to: String,

		@RequestParam(defaultValue = "0")
		@Min(value = 0, message = "monthlySalary must be greater than or equal to 0.")
		monthlySalary: Long,

		@RequestParam(defaultValue = "0")
		@Min(value = 0, message = "emergencyFund must be greater than or equal to 0.")
		emergencyFund: Long,

		@RequestParam(defaultValue = "0")
		@Min(value = 0, message = "savings must be greater than or equal to 0.")
		savings: Long,
	): ApiResponse<List<MonthlySimulationResponse>> {
		val command = monthlySimulationCommand(from, to, monthlySalary, emergencyFund, savings)
		return success(simulationService.simulateMonthly(command).map { it.toResponse() })
	}

	@PostMapping("/early-repayment")
	fun simulateEarlyRepayment(
		@Valid @RequestBody request: EarlyRepaymentSimulationRequest,
	): ApiResponse<EarlyRepaymentSimulationResponse> =
		success(simulationService.simulateEarlyRepayment(request.toCommand()).toResponse())

	@PostMapping("/loan-repayment")
	fun simulateLoanRepayment(
		@Valid @RequestBody request: LoanRepaymentSimulationRequest,
	): ApiResponse<LoanRepaymentSimulationResponse> =
		success(simulationFacade.simulateLoanRepayment(request.toCommand()).toResponse())
}
