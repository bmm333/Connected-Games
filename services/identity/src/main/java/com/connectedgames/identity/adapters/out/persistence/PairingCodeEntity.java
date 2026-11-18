package com.connectedgames.identity.adapters.out.persistence;

import com.connectedgames.identity.domain.device.PairingCode;
import com.connectedgames.identity.domain.device.Surface;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** Row of the "pairing_code" table. */
@Entity
@Table(name = "pairing_code")
class PairingCodeEntity {

    @Id
    private String code;
    private UUID venueId;
    private UUID tableId;
    @Enumerated(EnumType.STRING)
    private Surface surface;
    private Instant expiresAt;
    private Instant usedAt;

    protected PairingCodeEntity() {
        // required by JPA
    }

    static PairingCodeEntity from(PairingCode pairingCode) {
        PairingCodeEntity row = new PairingCodeEntity();
        row.code = pairingCode.value();
        row.venueId = pairingCode.venueId();
        row.tableId = pairingCode.tableId();
        row.surface = pairingCode.surface();
        row.expiresAt = pairingCode.expiresAt();
        row.usedAt = pairingCode.usedAt();
        return row;
    }

    PairingCode toDomain() {
        return new PairingCode(code, venueId, tableId, surface, expiresAt, usedAt);
    }
}
