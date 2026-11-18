package com.connectedgames.identity.application.port.in;

import com.connectedgames.identity.domain.device.PairingCode;
import com.connectedgames.identity.domain.device.Surface;
import com.connectedgames.identity.domain.user.UserId;

import java.util.UUID;

public interface CreatePairingCodeUseCase {

    /** Only the manager of the venue can ask for a code for one of its tables */
    PairingCode createPairingCode(UserId requester, UUID venueId, UUID tableId, Surface surface);
}
