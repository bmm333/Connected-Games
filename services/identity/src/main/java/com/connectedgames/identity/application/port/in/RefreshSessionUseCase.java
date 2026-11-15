package com.connectedgames.identity.application.port.in;

public interface RefreshSessionUseCase {

    /** @throws com.connectedgames.identity.application.InvalidRefreshTokenException if the token is unknown, already used or its session ended */
    ActiveSession refreshSession(String refreshToken);
}
