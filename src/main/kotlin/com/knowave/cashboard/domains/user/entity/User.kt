package com.knowave.cashboard.domains.user.entity

import com.knowave.cashboard.common.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "users")
class User(
	@Column(name = "provider", nullable = false, length = 20)
	var provider: String,

	@Column(name = "provider_id", nullable = false, length = 255)
	var providerId: String,

	@Column(name = "email", length = 255)
	var email: String?,

	@Column(name = "nickname", nullable = false, length = 255)
	var nickname: String,

	@Column(name = "profile_image_url", length = 255)
	var profileImageUrl: String?,

	@Column(name = "timezone", nullable = false, length = 50)
	var timezone: String,
) : BaseEntity()
