package com.knowave.cashboard.common.entity

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.UUID

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity(
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false, updatable = false)
	open val id: UUID? = null,

	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	open var createdAt: LocalDateTime? = null,

	@LastModifiedDate
	@Column(name = "updated_at", nullable = false)
	open var updatedAt: LocalDateTime? = null,
)
