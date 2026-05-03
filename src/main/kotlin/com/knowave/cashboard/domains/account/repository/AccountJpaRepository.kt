package com.knowave.cashboard.domains.account.repository

import com.knowave.cashboard.domains.account.entity.Account
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AccountJpaRepository : JpaRepository<Account, UUID>
