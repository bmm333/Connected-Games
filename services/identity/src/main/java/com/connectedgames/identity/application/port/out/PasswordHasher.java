package com.connectedgames.identity.application.port.out;

import com.connectedgames.identity.domain.user.PasswordHash;

public interface PasswordHasher {
    PasswordHash hash(String rawPassword);
    boolean matches(String rawPassword,PasswordHash hash);
}
