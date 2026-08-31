package com.knowave.cashboard.domains.simulation.config

import com.knowave.cashboard.domains.simulation.calculator.LoanRepaymentCalculator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SimulationConfig {
	@Bean
	fun loanRepaymentCalculator(): LoanRepaymentCalculator = LoanRepaymentCalculator()
}
