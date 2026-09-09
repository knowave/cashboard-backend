package com.knowave.cashboard.domains.account.repository

import com.knowave.cashboard.domains.account.entity.Account
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AccountJpaRepository : JpaRepository<Account, UUID> {
	fun findByIdAndUserId(id: UUID, userId: UUID): Account?
	fun findAllByUserId(userId: UUID): List<Account>
}
