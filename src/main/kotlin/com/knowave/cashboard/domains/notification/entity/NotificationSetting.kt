package com.knowave.cashboard.domains.notification.entity

import com.knowave.cashboard.common.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "notification_settings")
class NotificationSetting(
	@Column(nullable = false, unique = true, length = 50)
	var type: String,
	@Column(nullable = false)
	var enabled: Boolean,
) : BaseEntity()
