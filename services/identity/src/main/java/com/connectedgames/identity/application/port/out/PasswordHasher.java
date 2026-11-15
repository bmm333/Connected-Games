package com.connectedgames.identity.application.port.out;

import com.connectedgames.identity.domain.user.PasswordHash;

/** Slow salted hashing for passwords (bcrypt): two hashes of the same password differ, so use matches to compare */
public interface PasswordHasher {
    PasswordHash hash(String rawPassword);
    boolean matches(String rawPassword,PasswordHash hash);
}
