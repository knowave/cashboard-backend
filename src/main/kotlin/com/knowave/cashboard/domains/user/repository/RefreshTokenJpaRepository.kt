package com.knowave.cashboard.domains.user.repository

import com.knowave.cashboard.domains.user.entity.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

interface RefreshTokenJpaRepository : JpaRepository<RefreshToken, UUID> {
	fun existsByTokenHash(tokenHash: String): Boolean
	fun findByTokenHash(tokenHash: String): RefreshToken?

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	@Query("delete from RefreshToken rt where rt.userId = :userId")
	fun deleteAllByUserId(@Param("userId") userId: UUID)
}
