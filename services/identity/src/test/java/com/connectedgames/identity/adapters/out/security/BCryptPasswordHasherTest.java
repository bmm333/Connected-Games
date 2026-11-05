package com.connectedgames.identity.adapters.out.security;

import com.connectedgames.identity.domain.user.PasswordHash;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BCryptPasswordHasherTest {

    private final BCryptPasswordHasher hasher = new BCryptPasswordHasher();

    @Test
    void theRightPasswordMatchesItsHash() {
        PasswordHash hash = hasher.hash("password123");

        assertThat(hasher.matches("password123", hash)).isTrue();
        assertThat(hasher.matches("password124", hash)).isFalse();
    }

    @Test
    void theSamePasswordGivesADifferentHashEveryTime() {
        // a new random salt in every hash: equal passwords cannot be spotted in the table
        assertThat(hasher.hash("password123")).isNotEqualTo(hasher.hash("password123"));
    }

    @Test
    void neverStoresThePasswordInClear() {
        assertThat(hasher.hash("password123").value()).startsWith("$2").doesNotContain("password123");
    }

    @Test
    void acceptsTheHashOfTheDemoUsers() {
        PasswordHash demo = new PasswordHash("$2b$10$lgGqP1uRKcmJsvKVkhxMRuzWApi8keMOIo7cnWv8FplIZoaUx7xv.");

        assertThat(hasher.matches("password123", demo)).isTrue();
    }
}
