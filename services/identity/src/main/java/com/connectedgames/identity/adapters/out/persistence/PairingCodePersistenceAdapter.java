package com.connectedgames.identity.adapters.out.persistence;

import com.connectedgames.identity.application.port.out.PairingCodeRepository;
import com.connectedgames.identity.domain.device.PairingCode;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Implements the PairingCodeRepository port with JPA. */
@Component
class PairingCodePersistenceAdapter implements PairingCodeRepository {

    private final PairingCodeJpaRepository jpa;

    PairingCodePersistenceAdapter(PairingCodeJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<PairingCode> findByValueForUpdate(String value) {
        return jpa.findByCodeForUpdate(value).map(PairingCodeEntity::toDomain);
    }

    @Override
    public void save(PairingCode code) {
        jpa.save(PairingCodeEntity.from(code));
    }
}
