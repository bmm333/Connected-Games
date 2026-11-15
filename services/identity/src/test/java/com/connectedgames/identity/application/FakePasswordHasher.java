package com.connectedgames.identity.application;

import com.connectedgames.identity.application.port.out.PasswordHasher;
import com.connectedgames.identity.domain.user.PasswordHash;

/** Readable fake hash ("hashed:" + password) instead of a slow bcrypt */
class FakePasswordHasher implements PasswordHasher {

    @Override
    public PasswordHash hash(String rawPassword)
    {
        return new PasswordHash("hashed:" + rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, PasswordHash hash)
    {
        return hash.value().equals("hashed:" + rawPassword);
    }
}
