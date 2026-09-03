package com.knowave.cashboard.domains.notification.repository

import com.knowave.cashboard.domains.notification.entity.Notification
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

interface NotificationJpaRepository : JpaRepository<Notification, UUID> {
	fun findAllByReadAtIsNull(pageable: Pageable): Page<Notification>
	fun findAllByReadAtIsNotNull(pageable: Pageable): Page<Notification>
	fun countByReadAtIsNull(): Long

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	@Query("update Notification notification set notification.readAt = :now where notification.readAt is null")
	fun markAllRead(@Param("now") now: Instant): Int

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	@Query("update Notification notification set notification.readAt = :now where notification.id = :id and notification.readAt is null")
	fun markReadIfUnread(@Param("id") id: UUID, @Param("now") now: Instant): Int
}
