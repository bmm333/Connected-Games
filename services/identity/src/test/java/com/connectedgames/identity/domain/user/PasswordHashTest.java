package com.connectedgames.identity.domain.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class PasswordHashTest {

    private static final String BCRYPT = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    @Test
    void keepsTheHash() {
        assertThat(new PasswordHash(BCRYPT).value()).isEqualTo(BCRYPT);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void rejectsBlankHash(String value) {
        assertThatIllegalArgumentException().isThrownBy(() -> new PasswordHash(value));
    }

    @Test
    void rejectsNull() {
        assertThatNullPointerException().isThrownBy(() -> new PasswordHash(null));
    }

    @Test
    void doesNotPrintTheHash() {
        assertThat(new PasswordHash(BCRYPT).toString()).doesNotContain(BCRYPT);
    }
}
