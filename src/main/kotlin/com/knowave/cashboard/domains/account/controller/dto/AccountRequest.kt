package com.knowave.cashboard.domains.account.controller.dto

import com.knowave.cashboard.domains.account.service.dto.CreateAccountCommand
import com.knowave.cashboard.domains.account.service.dto.UpdateAccountCommand
import com.knowave.cashboard.domains.account.entity.AccountType
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

data class AccountRequest(
	@field:NotBlank(message = "name is required.")
	val name: String,

	@field:NotBlank(message = "type is required.")
	val type: String,

	@field:Min(value = 0, message = "balance must be greater than or equal to 0.")
	val balance: Long,
) {
	fun toCreateCommand(): CreateAccountCommand = CreateAccountCommand(
		name = name,
		type = AccountType.from(type),
		balance = balance,
	)

	fun toUpdateCommand(): UpdateAccountCommand = UpdateAccountCommand(
		name = name,
		type = AccountType.from(type),
		balance = balance,
	)
}
