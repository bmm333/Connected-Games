package com.connectedgames.identity.application.port.out;

import com.connectedgames.identity.domain.device.PairingCode;

import java.util.Optional;

public interface PairingCodeRepository {

    /** Locks the row until the end of the transaction: two displays with the same code cannot both win */
    Optional<PairingCode> findByValueForUpdate(String value);

    void save(PairingCode code);
}
