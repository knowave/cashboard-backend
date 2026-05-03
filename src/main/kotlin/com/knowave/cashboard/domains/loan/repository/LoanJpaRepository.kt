package com.knowave.cashboard.domains.loan.repository

import com.knowave.cashboard.domains.loan.entity.Loan
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface LoanJpaRepository : JpaRepository<Loan, UUID>
