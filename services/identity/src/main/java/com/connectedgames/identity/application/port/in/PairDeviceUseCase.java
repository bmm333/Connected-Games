package com.connectedgames.identity.application.port.in;

import com.connectedgames.identity.domain.device.Surface;

public interface PairDeviceUseCase {

    /** Consumes the code and starts the session of the display */
    DeviceSession pairDevice(String pairingCode, Surface surface);
}
