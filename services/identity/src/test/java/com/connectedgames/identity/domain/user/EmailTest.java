package com.connectedgames.identity.domain.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class EmailTest {

    private static final String DOMAIN = "@example.com";

    @Test
    void normalizesToLowerCaseWithoutSurroundingSpaces() {
        assertThat(new Email("  Mario.Rossi@Example.COM ").value()).isEqualTo("mario.rossi@example.com");
    }

    @Test
    void sameAddressWrittenDifferentlyIsEqual() {
        assertThat(new Email("Mario@Example.com")).isEqualTo(new Email("mario@example.com"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "mario", "mario@", "@example.com", "mario@example", "ma rio@example.com", "mario@@example.com"})
    void rejectsMalformedAddresses(String value) {
        assertThatIllegalArgumentException().isThrownBy(() -> new Email(value));
    }

    @Test
    void rejectsNull() {
        assertThatNullPointerException().isThrownBy(() -> new Email(null));
    }

    @Test
    void acceptsAddressesOf254Characters() {
        String value = "a".repeat(254 - DOMAIN.length()) + DOMAIN;

        assertThat(new Email(value).value()).hasSize(254);
    }

    @Test
    void rejectsAddressesLongerThan254Characters() {
        String value = "a".repeat(255 - DOMAIN.length()) + DOMAIN;

        assertThatIllegalArgumentException().isThrownBy(() -> new Email(value));
    }
}
