package com.connectedgames.identity.application.port.in;

import com.connectedgames.identity.domain.guest.Nickname;

public interface CreateGuestSessionUseCase {

    IssuedSession createGuestSession(Nickname nickname);
}
