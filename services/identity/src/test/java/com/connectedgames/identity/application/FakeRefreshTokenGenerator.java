package com.connectedgames.identity.application;

import com.connectedgames.identity.application.port.out.RefreshTokenGenerator;

/** Predictable tokens ("refresh-1", "refresh-2", ...) and a readable fake hash */
class FakeRefreshTokenGenerator implements RefreshTokenGenerator {
    private int generated = 0;

    @Override
    public String generate()
    {
        generated++;
        return "refresh-" + generated;
    }

    @Override
    public String hash(String token)
    {
        return "hash:" + token;
    }
}
