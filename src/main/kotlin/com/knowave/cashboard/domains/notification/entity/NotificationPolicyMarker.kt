package com.knowave.cashboard.domains.notification.entity

import com.knowave.cashboard.common.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "notification_policy_markers")
class NotificationPolicyMarker(
	@Column(name = "policy_key", nullable = false, unique = true, length = 255)
	val policyKey: String,
	@Column(name = "processed_at", nullable = false)
	val processedAt: Instant,
) : BaseEntity()
