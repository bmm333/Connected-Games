package com.connectedgames.identity.application.port.in;

import com.connectedgames.identity.domain.device.Device;

/** A paired display with its session: the contract returns both, the device and the tokens */
public record DeviceSession(Device device, IssuedSession session) implements ActiveSession {}
