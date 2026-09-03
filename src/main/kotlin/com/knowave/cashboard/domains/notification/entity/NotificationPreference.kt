package com.knowave.cashboard.domains.notification.entity

import com.knowave.cashboard.common.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "notification_preferences")
class NotificationPreference(
	@Column(name = "scope_key", nullable = false, unique = true, length = 50)
	val scopeKey: String = "SINGLE_USER",
	@Column(name = "push_enabled", nullable = false)
	var pushEnabled: Boolean = true,
) : BaseEntity()
