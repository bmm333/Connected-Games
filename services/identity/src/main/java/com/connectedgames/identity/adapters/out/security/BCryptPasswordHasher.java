package com.connectedgames.identity.adapters.out.security;

import com.connectedgames.identity.application.port.out.PasswordHasher;
import com.connectedgames.identity.domain.user.PasswordHash;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/** Implements PasswordHasher with bcrypt: slow on purpose, with a new random salt in every hash */
@Component
class BCryptPasswordHasher implements PasswordHasher {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public PasswordHash hash(String rawPassword) {
        return new PasswordHash(encoder.encode(rawPassword));
    }

    @Override
    public boolean matches(String rawPassword, PasswordHash hash) 
    {

        return encoder.matches(rawPassword, hash.value());
    }
}
