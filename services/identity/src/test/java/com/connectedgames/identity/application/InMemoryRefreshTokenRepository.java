package com.connectedgames.identity.application;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.connectedgames.identity.application.port.out.RefreshTokenRepository;
import com.connectedgames.identity.domain.session.RefreshToken;

class InMemoryRefreshTokenRepository implements RefreshTokenRepository {
    private final Map<String, RefreshToken> tokens = new HashMap<>();

    @Override
    public void save(RefreshToken token)
    {
        tokens.put(token.tokenHash(), token);
    }

    @Override
    public Optional<RefreshToken> findByHash(String tokenHash)
    {
        return Optional.ofNullable(tokens.get(tokenHash));
    }

    int count() { return tokens.size(); }
}
