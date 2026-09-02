package com.knowave.cashboard.domains.financialschedule.entity

import com.knowave.cashboard.common.entity.BaseEntity
import com.knowave.cashboard.common.exception.InvalidFinancialScheduleException
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "financial_schedules")
class FinancialSchedule private constructor(
	scheduleType: String,
	title: String,
	amount: Long,
	direction: String,
	recurrenceType: String,
	scheduledDate: LocalDate?,
	monthOfYear: Int?,
	dayOfMonth: Int?,
	startDate: LocalDate?,
	endDate: LocalDate?,
) : BaseEntity() {
	@Column(name = "schedule_type", nullable = false, length = 50)
	final var scheduleType: String = scheduleType
		private set

	@Column(name = "title", nullable = false, length = 100)
	final var title: String = title
		private set

	@Column(name = "amount", nullable = false)
	final var amount: Long = amount
		private set

	@Column(name = "direction", nullable = false, length = 20)
	final var direction: String = direction
		private set

	@Column(name = "recurrence_type", nullable = false, length = 20)
	final var recurrenceType: String = recurrenceType
		private set

	@Column(name = "scheduled_date")
	final var scheduledDate: LocalDate? = scheduledDate
		private set

	@Column(name = "month_of_year")
	final var monthOfYear: Int? = monthOfYear
		private set

	@Column(name = "day_of_month")
	final var dayOfMonth: Int? = dayOfMonth
		private set

	@Column(name = "start_date")
	final var startDate: LocalDate? = startDate
		private set

	@Column(name = "end_date")
	final var endDate: LocalDate? = endDate
		private set

	fun update(
		type: ScheduleType?,
		title: String?,
		amount: Long?,
		direction: CashFlowDirection?,
		recurrence: RecurrenceRule?,
	) {
		validateTitleAndAmount(title ?: this.title, amount ?: this.amount)
		type?.let { scheduleType = it.name }
		title?.let { this.title = it.trim() }
		amount?.let { this.amount = it }
		direction?.let { this.direction = it.name }
		recurrence?.let(::applyRecurrence)
	}

	private fun applyRecurrence(rule: RecurrenceRule) {
		scheduledDate = null
		monthOfYear = null
		dayOfMonth = null
		startDate = null
		endDate = null

		when (rule) {
			is RecurrenceRule.Once -> {
				recurrenceType = RecurrenceType.ONCE.name
				scheduledDate = rule.scheduledDate
			}
			is RecurrenceRule.Monthly -> {
				recurrenceType = RecurrenceType.MONTHLY.name
				dayOfMonth = rule.dayOfMonth
				startDate = rule.startDate
				endDate = rule.endDate
			}
			is RecurrenceRule.Yearly -> {
				recurrenceType = RecurrenceType.YEARLY.name
				monthOfYear = rule.monthOfYear
				dayOfMonth = rule.dayOfMonth
				startDate = rule.startDate
				endDate = rule.endDate
			}
		}
	}

	companion object {
		fun create(
			type: ScheduleType,
			title: String,
			amount: Long,
			direction: CashFlowDirection,
			recurrence: RecurrenceRule,
		): FinancialSchedule {
			validateTitleAndAmount(title, amount)
			return FinancialSchedule(
				scheduleType = type.name,
				title = title.trim(),
				amount = amount,
				direction = direction.name,
				recurrenceType = "",
				scheduledDate = null,
				monthOfYear = null,
				dayOfMonth = null,
				startDate = null,
				endDate = null,
			).apply { applyRecurrence(recurrence) }
		}

		private fun validateTitleAndAmount(title: String, amount: Long) {
			if (title.trim().isEmpty() || title.trim().length > 100) {
				throw InvalidFinancialScheduleException("title must contain between 1 and 100 characters.")
			}
			if (amount <= 0L) {
				throw InvalidFinancialScheduleException("amount must be greater than 0.")
			}
		}
	}
}
