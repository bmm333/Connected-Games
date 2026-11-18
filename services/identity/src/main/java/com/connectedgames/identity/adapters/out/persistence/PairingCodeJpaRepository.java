package com.connectedgames.identity.adapters.out.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

interface PairingCodeJpaRepository extends JpaRepository<PairingCodeEntity, String> {

    /** SELECT ... FOR UPDATE: whoever comes next waits for the end of this transaction */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from PairingCodeEntity c where c.code = :code")
    Optional<PairingCodeEntity> findByCodeForUpdate(@Param("code") String code);
}
