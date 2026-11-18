package com.connectedgames.identity.adapters.out.security;

import com.connectedgames.identity.application.port.out.PairingCodeGenerator;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/** Implements PairingCodeGenerator 6 characters of A-Z and 0-9 from a SecureRandom*/
@Component
class SecureRandomPairingCodeGenerator implements PairingCodeGenerator {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int LENGTH = 6;

    private final SecureRandom random = new SecureRandom();

    @Override
    public String generate() {
        StringBuilder code = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            code.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return code.toString();
    }
}
