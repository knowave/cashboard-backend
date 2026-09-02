package com.knowave.cashboard.domains.financialschedule.service

import com.knowave.cashboard.common.exception.EmptyFinancialSchedulePatchException
import com.knowave.cashboard.common.exception.FinancialScheduleDataIntegrityException
import com.knowave.cashboard.common.exception.InvalidEnumValueException
import com.knowave.cashboard.common.exception.InvalidFinancialScheduleException
import com.knowave.cashboard.common.exception.NotFoundException
import com.knowave.cashboard.domains.financialschedule.entity.CashFlowDirection
import com.knowave.cashboard.domains.financialschedule.entity.FinancialSchedule
import com.knowave.cashboard.domains.financialschedule.entity.RecurrenceType
import com.knowave.cashboard.domains.financialschedule.entity.ScheduleType
import com.knowave.cashboard.domains.financialschedule.repository.FinancialScheduleRepository
import com.knowave.cashboard.domains.financialschedule.service.dto.CreateFinancialScheduleCommand
import com.knowave.cashboard.domains.financialschedule.service.dto.FinancialScheduleResult
import com.knowave.cashboard.domains.financialschedule.service.dto.PatchField
import com.knowave.cashboard.domains.financialschedule.service.dto.PatchFinancialScheduleCommand
import com.knowave.cashboard.domains.financialschedule.service.dto.toRecurrenceRule
import com.knowave.cashboard.domains.financialschedule.service.dto.toResult
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.slf4j.LoggerFactory
import java.util.UUID

@Service
@Transactional(readOnly = true)
class FinancialScheduleServiceImpl(
	private val financialScheduleRepository: FinancialScheduleRepository,
) : FinancialScheduleService {
	@Transactional
	override fun create(command: CreateFinancialScheduleCommand): FinancialScheduleResult {
		val schedule = FinancialSchedule.create(
			type = ScheduleType.from(command.type),
			title = command.title,
			amount = command.amount,
			direction = CashFlowDirection.from(command.direction),
			recurrence = command.recurrence.toRecurrenceRule(),
		)
		return toResult(financialScheduleRepository.save(schedule))
	}

	override fun get(id: UUID): FinancialScheduleResult = toResult(findSchedule(id))

	override fun getAll(): List<FinancialScheduleResult> =
		financialScheduleRepository.findAllOrderByCreatedAtDesc().map(::toResult)

	@Transactional
	override fun patch(id: UUID, command: PatchFinancialScheduleCommand): FinancialScheduleResult {
		if (command.isEmpty()) {
			throw EmptyFinancialSchedulePatchException()
		}
		val type = command.type.presentValue("type")?.let(ScheduleType::from)
		val title = command.title.presentValue("title")
		val amount = command.amount.presentValue("amount")
		val direction = command.direction.presentValue("direction")?.let(CashFlowDirection::from)
		val recurrence = command.recurrence.presentValue("recurrence")?.toRecurrenceRule()
		val schedule = findSchedule(id)
		schedule.update(
			type = type,
			title = title,
			amount = amount,
			direction = direction,
			recurrence = recurrence,
		)
		return toResult(financialScheduleRepository.save(schedule))
	}

	@Transactional
	override fun delete(id: UUID) {
		financialScheduleRepository.delete(findSchedule(id))
	}

	private fun findSchedule(id: UUID): FinancialSchedule =
		financialScheduleRepository.findById(id) ?: throw NotFoundException("FinancialSchedule", id)

	private fun toResult(schedule: FinancialSchedule): FinancialScheduleResult {
		validatePersistedEnum(schedule, "scheduleType", schedule.scheduleType) { ScheduleType.from(it) }
		validatePersistedEnum(schedule, "direction", schedule.direction) { CashFlowDirection.from(it) }
		validatePersistedEnum(schedule, "recurrenceType", schedule.recurrenceType) { RecurrenceType.from(it) }
		return schedule.toResult()
	}

	private fun <T> validatePersistedEnum(
		schedule: FinancialSchedule,
		field: String,
		value: String,
		convert: (String) -> T,
	) {
		try {
			convert(value)
		} catch (exception: InvalidEnumValueException) {
			val scheduleId = requireNotNull(schedule.id)
			logger.error(
				"FinancialSchedule persisted enum is invalid. scheduleId={}, field={}, value={}",
				scheduleId,
				field,
				value,
				exception,
			)
			throw FinancialScheduleDataIntegrityException(scheduleId, field, exception)
		}
	}

	private fun PatchFinancialScheduleCommand.isEmpty(): Boolean =
		type is PatchField.Absent &&
			title is PatchField.Absent &&
			amount is PatchField.Absent &&
			direction is PatchField.Absent &&
			recurrence is PatchField.Absent

	private fun <T> PatchField<T>.presentValue(field: String): T? = when (this) {
		PatchField.Absent -> null
		is PatchField.Present -> value ?: throw InvalidFinancialScheduleException("$field must not be null.")
	}

	private companion object {
		val logger = LoggerFactory.getLogger(FinancialScheduleServiceImpl::class.java)
	}
}
