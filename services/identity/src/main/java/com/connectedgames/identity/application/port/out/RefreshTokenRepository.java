package com.connectedgames.identity.application.port.out;

import java.util.Optional;

import com.connectedgames.identity.domain.session.RefreshToken;

public interface RefreshTokenRepository{
    void save(RefreshToken token);
    Optional<RefreshToken> findByHash(String tokenHash);
}
