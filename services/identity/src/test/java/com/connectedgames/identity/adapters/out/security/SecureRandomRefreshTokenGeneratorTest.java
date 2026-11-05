package com.connectedgames.identity.adapters.out.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SecureRandomRefreshTokenGeneratorTest {

    private final SecureRandomRefreshTokenGenerator generator = new SecureRandomRefreshTokenGenerator();

    @Test
    void generatesUrlSafeTokensOf32Bytes() {
        String token = generator.generate();

        // 32 bytes are 43 base64 characters without the "=" padding
        assertThat(token).hasSize(43).matches("[A-Za-z0-9_-]+");
    }

    @Test
    void neverGeneratesTheSameTokenTwice() {
        assertThat(generator.generate()).isNotEqualTo(generator.generate());
    }

    @Test
    void theSameTokenAlwaysHasTheSameHash() {
        String token = generator.generate();

        assertThat(generator.hash(token)).isEqualTo(generator.hash(token));
    }

    @Test
    void hashesWithSha256InHex() {
        // well-known SHA-256 of "test"
        assertThat(generator.hash("test"))
                .isEqualTo("9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08");
    }
}
