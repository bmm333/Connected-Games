package com.connectedgames.identity.application.port.in;

import com.connectedgames.identity.domain.guest.Nickname;

/** Session for whoever scans a table QR code and plays without an account */
public interface CreateGuestSessionUseCase {

    GuestSession createGuestSession(Nickname nickname);
}
