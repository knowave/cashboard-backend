package com.knowave.cashboard.domains.budget.service

import com.knowave.cashboard.common.exception.BudgetExpenseNotFoundException
import com.knowave.cashboard.common.exception.DuplicateMonthlyBudgetException
import com.knowave.cashboard.common.exception.InvalidBudgetExpenseException
import com.knowave.cashboard.common.exception.InvalidTargetMonthException
import com.knowave.cashboard.common.exception.MonthlyBudgetNotFoundException
import com.knowave.cashboard.domains.budget.entity.BudgetExpense
import com.knowave.cashboard.domains.budget.entity.BudgetStatus
import com.knowave.cashboard.domains.budget.entity.MonthlyBudget
import com.knowave.cashboard.domains.budget.repository.BudgetExpenseRepository
import com.knowave.cashboard.domains.budget.repository.MonthlyBudgetRepository
import com.knowave.cashboard.domains.budget.service.dto.BudgetExpenseResult
import com.knowave.cashboard.domains.budget.service.dto.CreateBudgetExpenseCommand
import com.knowave.cashboard.domains.budget.service.dto.CreateMonthlyBudgetCommand
import com.knowave.cashboard.domains.budget.service.dto.MonthlyBudgetResult
import com.knowave.cashboard.domains.budget.service.dto.UpdateMonthlyBudgetCommand
import com.knowave.cashboard.domains.budget.service.dto.UpdateUsedAmountCommand
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class BudgetStrategyServiceImpl(
	private val monthlyBudgetRepository: MonthlyBudgetRepository,
	private val budgetExpenseRepository: BudgetExpenseRepository,
) : BudgetStrategyService {
	override fun create(command: CreateMonthlyBudgetCommand): MonthlyBudgetResult {
		validateTargetMonth(command.targetMonth)
		if (monthlyBudgetRepository.existsByTargetMonth(command.targetMonth)) {
			throw DuplicateMonthlyBudgetException(command.targetMonth)
		}

		val monthlyBudget = command.toEntity()

		return monthlyBudgetRepository.save(monthlyBudget).toMonthlyBudgetResult()
	}

	override fun getByTargetMonth(targetMonth: String): MonthlyBudgetResult {
		validateTargetMonth(targetMonth)
		val monthlyBudget = monthlyBudgetRepository.findByTargetMonth(targetMonth)
			?: throw MonthlyBudgetNotFoundException(targetMonth)
		return monthlyBudget.toMonthlyBudgetResult()
	}

	override fun update(id: UUID, command: UpdateMonthlyBudgetCommand): MonthlyBudgetResult {
		validateTargetMonth(command.targetMonth)
		val monthlyBudget = monthlyBudgetRepository.findById(id)
			?: throw MonthlyBudgetNotFoundException(id)
		val existingBudget = monthlyBudgetRepository.findByTargetMonth(command.targetMonth)

		if (existingBudget != null && existingBudget.id != id) {
			throw DuplicateMonthlyBudgetException(command.targetMonth)
		}

		val updatedMonthlyBudget = MonthlyBudget.applyUpdate(monthlyBudget, command)

		return monthlyBudgetRepository.save(updatedMonthlyBudget).toMonthlyBudgetResult()
	}

	override fun updateUsedAmount(id: UUID, command: UpdateUsedAmountCommand): MonthlyBudgetResult {
		val monthlyBudget = monthlyBudgetRepository.findById(id)
			?: throw MonthlyBudgetNotFoundException(id)
		monthlyBudget.updateUsedAmount(command.usedAmount)
		return monthlyBudgetRepository.save(monthlyBudget).toMonthlyBudgetResult()
	}

	@Transactional
	override fun addExpense(id: UUID, command: CreateBudgetExpenseCommand): MonthlyBudgetResult {
		val monthlyBudget = monthlyBudgetRepository.findById(id)
			?: throw MonthlyBudgetNotFoundException(id)
		val budgetExpense = command.toEntity(monthlyBudget)

		budgetExpenseRepository.save(budgetExpense)
		monthlyBudget.addUsedAmount(command.amount)
		return monthlyBudgetRepository.save(monthlyBudget).toMonthlyBudgetResult()
	}

	override fun getExpenses(id: UUID): List<BudgetExpenseResult> {
		if (!monthlyBudgetRepository.existsById(id)) {
			throw MonthlyBudgetNotFoundException(id)
		}
		return budgetExpenseRepository.findAllByMonthlyBudgetIdOrderBySpentAtDesc(id)
			.map { it.toBudgetExpenseResult() }
	}

	@Transactional
	override fun deleteExpense(monthlyBudgetId: UUID, expenseId: UUID): Boolean {
		val monthlyBudget = monthlyBudgetRepository.findById(monthlyBudgetId)
			?: throw MonthlyBudgetNotFoundException(monthlyBudgetId)
		val budgetExpense = budgetExpenseRepository.findById(expenseId)
			?: throw BudgetExpenseNotFoundException(expenseId)

		if (budgetExpense.monthlyBudget.id != monthlyBudgetId) {
			throw InvalidBudgetExpenseException(expenseId, monthlyBudgetId)
		}

		monthlyBudget.subtractUsedAmount(budgetExpense.amount)
		monthlyBudgetRepository.save(monthlyBudget)
		budgetExpenseRepository.delete(budgetExpense)
		return true
	}

	private fun MonthlyBudget.toMonthlyBudgetResult(): MonthlyBudgetResult {
		val remainingAmount = monthlyBudget - usedAmount
		val remainingDays = calculateRemainingDays(targetMonth)
		val dailyAvailableAmount = remainingAmount / remainingDays
		val weeklyAvailableAmount = dailyAvailableAmount * 7
		val status = calculateStatus(dailyAvailableAmount)

		return MonthlyBudgetResult(
			id = requireNotNull(id),
			targetMonth = targetMonth,
			monthlyBudget = monthlyBudget,
			usedAmount = usedAmount,
			remainingAmount = remainingAmount,
			remainingDays = remainingDays,
			dailyAvailableAmount = dailyAvailableAmount,
			weeklyAvailableAmount = weeklyAvailableAmount,
			status = status,
			strategyMessage = status.strategyMessage,
			createdAt = requireNotNull(createdAt),
			updatedAt = requireNotNull(updatedAt),
		)
	}

	private fun BudgetExpense.toBudgetExpenseResult(): BudgetExpenseResult = BudgetExpenseResult(
		id = requireNotNull(id),
		monthlyBudgetId = requireNotNull(monthlyBudget.id),
		amount = amount,
		category = category,
		memo = memo,
		spentAt = spentAt,
		createdAt = requireNotNull(createdAt),
		updatedAt = requireNotNull(updatedAt),
	)

	private fun validateTargetMonth(targetMonth: String) {
		if (!TARGET_MONTH_PATTERN.matches(targetMonth)) {
			throw InvalidTargetMonthException(targetMonth)
		}

		try {
			YearMonth.parse(targetMonth)
		} catch (exception: DateTimeParseException) {
			throw InvalidTargetMonthException(targetMonth)
		}
	}

	private fun calculateRemainingDays(targetMonth: String): Int {
		val today = LocalDate.now()
		val currentMonth = YearMonth.from(today)
		val target = YearMonth.parse(targetMonth)
		val remainingDays = when {
			target == currentMonth -> ChronoUnit.DAYS.between(today, target.atEndOfMonth()).toInt() + 1
			target.isAfter(currentMonth) -> target.lengthOfMonth()
			else -> 0
		}
		return remainingDays.coerceAtLeast(1)
	}

	private fun calculateStatus(dailyAvailableAmount: Long): BudgetStatus = when {
		dailyAvailableAmount <= 15_000L -> BudgetStatus.EMERGENCY
		dailyAvailableAmount <= 25_000L -> BudgetStatus.DANGER
		dailyAvailableAmount <= 35_000L -> BudgetStatus.CAUTION
		dailyAvailableAmount <= 50_000L -> BudgetStatus.STABLE
		else -> BudgetStatus.GOOD
	}

	private companion object {
		val TARGET_MONTH_PATTERN = Regex("\\d{4}-\\d{2}")
	}
}
