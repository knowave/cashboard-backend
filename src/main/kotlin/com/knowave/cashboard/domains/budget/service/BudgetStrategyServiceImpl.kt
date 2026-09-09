package com.knowave.cashboard.domains.budget.service

import com.knowave.cashboard.common.exception.BudgetExpenseNotFoundException
import com.knowave.cashboard.common.exception.DuplicateMonthlyBudgetException
import com.knowave.cashboard.common.exception.InvalidBudgetExpenseException
import com.knowave.cashboard.common.exception.InvalidTargetMonthException
import com.knowave.cashboard.common.exception.MonthlyBudgetNotFoundException
import com.knowave.cashboard.domains.budget.entity.BudgetExpense
import com.knowave.cashboard.domains.budget.entity.BudgetStatus
import com.knowave.cashboard.domains.budget.entity.MonthlyBudget
import com.knowave.cashboard.domains.budget.event.BudgetUsageChangedEvent
import com.knowave.cashboard.domains.budget.repository.BudgetExpenseRepository
import com.knowave.cashboard.domains.budget.repository.MonthlyBudgetRepository
import com.knowave.cashboard.domains.budget.service.dto.BudgetExpenseResult
import com.knowave.cashboard.domains.budget.service.dto.CreateBudgetExpenseCommand
import com.knowave.cashboard.domains.budget.service.dto.CreateMonthlyBudgetCommand
import com.knowave.cashboard.domains.budget.service.dto.MonthlyBudgetResult
import com.knowave.cashboard.domains.budget.service.dto.UpdateMonthlyBudgetCommand
import com.knowave.cashboard.domains.budget.service.dto.UpdateUsedAmountCommand
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.Clock
import java.time.YearMonth
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class BudgetStrategyServiceImpl(
	private val monthlyBudgetRepository: MonthlyBudgetRepository,
	private val budgetExpenseRepository: BudgetExpenseRepository,
	private val eventPublisher: ApplicationEventPublisher,
	private val clock: Clock,
) : BudgetStrategyService {
	@Transactional
	override fun create(userId: UUID, command: CreateMonthlyBudgetCommand): MonthlyBudgetResult {
		validateTargetMonth(command.targetMonth)
		if (monthlyBudgetRepository.existsByTargetMonthAndUserId(command.targetMonth, userId)) {
			throw DuplicateMonthlyBudgetException(command.targetMonth)
		}

		val monthlyBudget = command.toEntity(userId)

		val saved = monthlyBudgetRepository.save(monthlyBudget)
		publishUsageChanged(userId, saved, saved.monthlyBudget, 0L)
		return saved.toMonthlyBudgetResult()
	}

	override fun getByTargetMonth(userId: UUID, targetMonth: String): MonthlyBudgetResult {
		validateTargetMonth(targetMonth)
		val monthlyBudget = monthlyBudgetRepository.findByTargetMonthAndUserId(targetMonth, userId)
			?: throw MonthlyBudgetNotFoundException(targetMonth)
		return monthlyBudget.toMonthlyBudgetResult()
	}

	@Transactional
	override fun update(userId: UUID, id: UUID, command: UpdateMonthlyBudgetCommand): MonthlyBudgetResult {
		validateTargetMonth(command.targetMonth)
		val monthlyBudget = monthlyBudgetRepository.findByIdForUpdate(id, userId)
			?: throw MonthlyBudgetNotFoundException(id)
		val existingBudget = monthlyBudgetRepository.findByTargetMonthAndUserId(command.targetMonth, userId)

		if (existingBudget != null && existingBudget.id != id) {
			throw DuplicateMonthlyBudgetException(command.targetMonth)
		}

		val previousBudget = monthlyBudget.monthlyBudget
		val previousUsed = monthlyBudget.usedAmount
		val updatedMonthlyBudget = MonthlyBudget.applyUpdate(monthlyBudget, command)

		val saved = monthlyBudgetRepository.save(updatedMonthlyBudget)
		publishUsageChanged(userId, saved, previousBudget, previousUsed)
		return saved.toMonthlyBudgetResult()
	}

	@Transactional
	override fun updateUsedAmount(userId: UUID, id: UUID, command: UpdateUsedAmountCommand): MonthlyBudgetResult {
		val monthlyBudget = monthlyBudgetRepository.findByIdForUpdate(id, userId)
			?: throw MonthlyBudgetNotFoundException(id)
		val previousBudget = monthlyBudget.monthlyBudget
		val previousUsed = monthlyBudget.usedAmount
		monthlyBudget.updateUsedAmount(command.usedAmount)
		val saved = monthlyBudgetRepository.save(monthlyBudget)
		publishUsageChanged(userId, saved, previousBudget, previousUsed)
		return saved.toMonthlyBudgetResult()
	}

	@Transactional
	override fun addExpense(userId: UUID, id: UUID, command: CreateBudgetExpenseCommand): MonthlyBudgetResult {
		val monthlyBudget = monthlyBudgetRepository.findByIdForUpdate(id, userId)
			?: throw MonthlyBudgetNotFoundException(id)
		val previousBudget = monthlyBudget.monthlyBudget
		val previousUsed = monthlyBudget.usedAmount
		val budgetExpense = command.toEntity(monthlyBudget)

		budgetExpenseRepository.save(budgetExpense)
		monthlyBudget.addUsedAmount(command.amount)
		val saved = monthlyBudgetRepository.save(monthlyBudget)
		publishUsageChanged(userId, saved, previousBudget, previousUsed)
		return saved.toMonthlyBudgetResult()
	}

	override fun getExpenses(userId: UUID, id: UUID): List<BudgetExpenseResult> {
		if (!monthlyBudgetRepository.existsByIdAndUserId(id, userId)) {
			throw MonthlyBudgetNotFoundException(id)
		}
		return budgetExpenseRepository.findAllByMonthlyBudgetIdAndUserIdOrderBySpentAtDesc(id, userId)
			.map { it.toBudgetExpenseResult() }
	}

	@Transactional
	override fun deleteExpense(userId: UUID, monthlyBudgetId: UUID, expenseId: UUID): Boolean {
		val monthlyBudget = monthlyBudgetRepository.findByIdForUpdate(monthlyBudgetId, userId)
			?: throw MonthlyBudgetNotFoundException(monthlyBudgetId)
		val budgetExpense = budgetExpenseRepository.findByIdAndUserId(expenseId, userId)
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

	private fun publishUsageChanged(userId: UUID, saved: MonthlyBudget, previousBudget: Long, previousUsed: Long) {
		eventPublisher.publishEvent(
			BudgetUsageChangedEvent(
				userId = userId,
				monthlyBudgetId = requireNotNull(saved.id),
				previousBudgetAmount = previousBudget,
				previousUsedAmount = previousUsed,
				currentBudgetAmount = saved.monthlyBudget,
				currentUsedAmount = saved.usedAmount,
				occurredAt = clock.instant(),
			),
		)
	}

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
