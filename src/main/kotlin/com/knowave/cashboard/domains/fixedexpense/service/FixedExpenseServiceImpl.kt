package com.knowave.cashboard.domains.fixedexpense.service

import com.knowave.cashboard.common.exception.CashboardException
import com.knowave.cashboard.common.exception.NotFoundException
import com.knowave.cashboard.domains.fixedexpense.service.dto.CreateFixedExpenseCommand
import com.knowave.cashboard.domains.fixedexpense.service.dto.FixedExpenseResult
import com.knowave.cashboard.domains.fixedexpense.service.dto.UpdateFixedExpenseCommand
import com.knowave.cashboard.domains.fixedexpense.service.dto.toResult
import com.knowave.cashboard.domains.fixedexpense.repository.FixedExpenseRepository
import org.springframework.stereotype.Service
import java.time.YearMonth
import java.util.UUID

@Service
class FixedExpenseServiceImpl(
	private val fixedExpenseRepository: FixedExpenseRepository,
) : FixedExpenseService {
	override fun create(command: CreateFixedExpenseCommand): FixedExpenseResult {
		validatePeriod(command.startMonth, command.endMonth)
		return fixedExpenseRepository.save(command.toEntity()).toResult()
	}

	override fun get(id: UUID): FixedExpenseResult {
		val fixedExpense = fixedExpenseRepository.findById(id) ?: throw NotFoundException("FixedExpense", id)
		return fixedExpense.toResult()
	}

	override fun getAll(): List<FixedExpenseResult> = fixedExpenseRepository.findAll().map { it.toResult() }

	override fun update(id: UUID, command: UpdateFixedExpenseCommand): FixedExpenseResult {
		validatePeriod(command.startMonth, command.endMonth)
		val fixedExpense = fixedExpenseRepository.findById(id) ?: throw NotFoundException("FixedExpense", id)
		fixedExpense.update(command.name, command.amount, command.category, command.startMonth, command.endMonth)
		return fixedExpenseRepository.save(fixedExpense).toResult()
	}

	override fun delete(id: UUID) {
		val fixedExpense = fixedExpenseRepository.findById(id) ?: throw NotFoundException("FixedExpense", id)
		fixedExpenseRepository.delete(fixedExpense)
	}

	private fun validatePeriod(startMonth: YearMonth, endMonth: YearMonth?) {
		if (endMonth != null && endMonth.isBefore(startMonth)) {
			throw CashboardException("INVALID_PERIOD", "endMonth must be greater than or equal to startMonth.")
		}
	}
}
