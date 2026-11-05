package com.connectedgames.identity.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenEntity, String> {

    /** @return 1 if this call marked the token, 0 if it was already used */
    @Transactional
    @Modifying
    @Query("update RefreshTokenEntity t set t.usedAt = :usedAt where t.tokenHash = :tokenHash and t.usedAt is null")
    int markUsed(@Param("tokenHash") String tokenHash, @Param("usedAt") Instant usedAt);
}
