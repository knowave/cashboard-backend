package com.knowave.cashboard.domains.notification.entity

import com.knowave.cashboard.common.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "notification_settings")
class NotificationSetting(
	@Column(name = "user_id", nullable = false, updatable = false)
	val userId: UUID,
	@Column(nullable = false, unique = true, length = 50)
	var type: String,
	@Column(nullable = false)
	var enabled: Boolean,
) : BaseEntity()
