package com.knowave.cashboard.domains.budget.service

import com.knowave.cashboard.common.entity.BaseEntity
import com.knowave.cashboard.domains.budget.entity.BudgetExpense
import com.knowave.cashboard.domains.budget.entity.MonthlyBudget
import com.knowave.cashboard.domains.budget.event.BudgetUsageChangedEvent
import com.knowave.cashboard.domains.budget.repository.BudgetExpenseRepository
import com.knowave.cashboard.domains.budget.repository.MonthlyBudgetRepository
import com.knowave.cashboard.domains.budget.service.dto.CreateBudgetExpenseCommand
import com.knowave.cashboard.domains.budget.service.dto.CreateMonthlyBudgetCommand
import com.knowave.cashboard.domains.budget.service.dto.UpdateMonthlyBudgetCommand
import com.knowave.cashboard.domains.budget.service.dto.UpdateUsedAmountCommand
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

class BudgetStrategyServiceImplTest {
	private val operationTrace = mutableListOf<String>()
	private val monthlyBudgetRepository = FakeMonthlyBudgetRepository(operationTrace)
	private val budgetExpenseRepository = FakeBudgetExpenseRepository(operationTrace)
	private val eventPublisher = RecordingEventPublisher()
	private val clock = Clock.fixed(Instant.parse("2026-09-02T00:00:00Z"), ZoneOffset.UTC)
	private val service = BudgetStrategyServiceImpl(monthlyBudgetRepository, budgetExpenseRepository, eventPublisher, clock)
	private val budgetId = UUID.fromString("22222222-2222-2222-2222-222222222222")

	@BeforeEach
	fun setUp() {
		monthlyBudgetRepository.save(MonthlyBudget("2026-09", 100, 70).withId(budgetId))
		operationTrace.clear()
	}

	@Test
	fun `예산 생성은 초기 사용액과 함께 이벤트를 발행한다`() {
		val result = service.create(CreateMonthlyBudgetCommand("2026-10", 100, 80))

		assertThat(eventPublisher.events.single()).isEqualTo(
			BudgetUsageChangedEvent(result.id, 100, 0, 100, 80, clock.instant()),
		)
	}

	@Test
	fun `예산 수정은 저장 전후 금액 이벤트를 발행한다`() {
		service.update(budgetId, UpdateMonthlyBudgetCommand("2026-09", 120, 96))

		assertThat(eventPublisher.events.single()).isEqualTo(
			BudgetUsageChangedEvent(budgetId, 100, 70, 120, 96, clock.instant()),
		)
	}

	@Test
	fun `예산 수정은 변경 전 비관적 잠금 조회를 사용한다`() {
		service.update(budgetId, UpdateMonthlyBudgetCommand("2026-09", 120, 96))

		assertThat(monthlyBudgetRepository.lockedLookupCount).isEqualTo(1)
		assertThat(operationTrace).containsExactly("monthlyBudget.lock", "monthlyBudget.save")
	}

	@Test
	fun `직접 사용액 변경은 저장 전후 사용액 이벤트를 발행한다`() {
		service.updateUsedAmount(budgetId, UpdateUsedAmountCommand(95))

		assertThat(eventPublisher.events.single()).isEqualTo(
			BudgetUsageChangedEvent(budgetId, 100, 70, 100, 95, clock.instant()),
		)
		assertThat(operationTrace).containsExactly("monthlyBudget.lock", "monthlyBudget.save")
	}

	@Test
	fun `지출 추가는 저장 전후 사용액 이벤트를 발행한다`() {
		service.addExpense(budgetId, CreateBudgetExpenseCommand(25, "식비", null, LocalDate.of(2026, 9, 2)))

		assertThat(eventPublisher.events.single()).isEqualTo(
			BudgetUsageChangedEvent(budgetId, 100, 70, 100, 95, clock.instant()),
		)
		assertThat(operationTrace).containsExactly("monthlyBudget.lock", "budgetExpense.save", "monthlyBudget.save")
	}

	@Test
	fun `지출 삭제는 사용액 이벤트를 발행하지 않는다`() {
		val budget = monthlyBudgetRepository.findById(budgetId)!!
		val expense = budgetExpenseRepository.save(BudgetExpense(budget, 25, null, null, LocalDate.of(2026, 9, 2)))
		operationTrace.clear()

		service.deleteExpense(budgetId, requireNotNull(expense.id))

		assertThat(eventPublisher.events).isEmpty()
		assertThat(operationTrace).containsExactly("monthlyBudget.lock", "budgetExpense.find", "monthlyBudget.save", "budgetExpense.delete")
	}
}

private class RecordingEventPublisher : ApplicationEventPublisher {
	val events = mutableListOf<BudgetUsageChangedEvent>()
	override fun publishEvent(event: Any) {
		if (event is BudgetUsageChangedEvent) events += event
	}
}

private class FakeMonthlyBudgetRepository(
	private val operationTrace: MutableList<String>,
) : MonthlyBudgetRepository {
	private val budgets = linkedMapOf<UUID, MonthlyBudget>()
	var lockedLookupCount = 0
	override fun save(monthlyBudget: MonthlyBudget): MonthlyBudget {
		operationTrace += "monthlyBudget.save"
		if (monthlyBudget.id == null) monthlyBudget.withId()
		budgets[requireNotNull(monthlyBudget.id)] = monthlyBudget
		return monthlyBudget
	}
	override fun findById(id: UUID): MonthlyBudget? {
		operationTrace += "monthlyBudget.find"
		return budgets[id]
	}
	override fun findByIdForUpdate(id: UUID): MonthlyBudget? {
		operationTrace += "monthlyBudget.lock"
		lockedLookupCount += 1
		return budgets[id]
	}
	override fun findByTargetMonth(targetMonth: String): MonthlyBudget? = budgets.values.firstOrNull { it.targetMonth == targetMonth }
	override fun existsById(id: UUID): Boolean = id in budgets
	override fun existsByTargetMonth(targetMonth: String): Boolean = budgets.values.any { it.targetMonth == targetMonth }
}

private class FakeBudgetExpenseRepository(
	private val operationTrace: MutableList<String>,
) : BudgetExpenseRepository {
	private val expenses = linkedMapOf<UUID, BudgetExpense>()
	override fun save(budgetExpense: BudgetExpense): BudgetExpense {
		operationTrace += "budgetExpense.save"
		if (budgetExpense.id == null) budgetExpense.withId()
		expenses[requireNotNull(budgetExpense.id)] = budgetExpense
		return budgetExpense
	}
	override fun findById(id: UUID): BudgetExpense? {
		operationTrace += "budgetExpense.find"
		return expenses[id]
	}
	override fun findAllByMonthlyBudgetIdOrderBySpentAtDesc(monthlyBudgetId: UUID): List<BudgetExpense> =
		expenses.values.filter { it.monthlyBudget.id == monthlyBudgetId }.sortedByDescending { it.spentAt }
	override fun delete(budgetExpense: BudgetExpense) {
		operationTrace += "budgetExpense.delete"
		expenses.remove(budgetExpense.id)
	}
}

private fun <T : BaseEntity> T.withId(id: UUID = UUID.randomUUID()): T {
	BaseEntity::class.java.getDeclaredField("id").apply { isAccessible = true; set(this@withId, id) }
	BaseEntity::class.java.getDeclaredField("createdAt").apply { isAccessible = true; set(this@withId, LocalDateTime.of(2026, 9, 2, 0, 0)) }
	BaseEntity::class.java.getDeclaredField("updatedAt").apply { isAccessible = true; set(this@withId, LocalDateTime.of(2026, 9, 2, 0, 0)) }
	return this
}
