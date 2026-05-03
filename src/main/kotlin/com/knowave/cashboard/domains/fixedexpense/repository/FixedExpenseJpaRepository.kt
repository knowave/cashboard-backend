package com.knowave.cashboard.domains.fixedexpense.repository

import com.knowave.cashboard.domains.fixedexpense.entity.FixedExpense
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface FixedExpenseJpaRepository : JpaRepository<FixedExpense, UUID>
