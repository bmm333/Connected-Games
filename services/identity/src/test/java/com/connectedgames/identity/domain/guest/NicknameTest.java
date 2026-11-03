package com.connectedgames.identity.domain.guest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class NicknameTest {

    @Test
    void stripsSurroundingSpaces() {
        assertThat(new Nickname("  Bomber  ").value()).isEqualTo("Bomber");
    }

    @Test
    void acceptsTheShortestNickname() {
        assertThat(new Nickname("Bo").value()).isEqualTo("Bo");
    }

    @Test
    void acceptsTheLongestNickname() {
        String value = "a".repeat(24);

        assertThat(new Nickname(value).value()).isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "B", "  B  "})
    void rejectsNicknamesShorterThan2Characters(String value) {
        assertThatIllegalArgumentException().isThrownBy(() -> new Nickname(value));
    }

    @Test
    void rejectsNicknamesLongerThan24Characters() {
        assertThatIllegalArgumentException().isThrownBy(() -> new Nickname("a".repeat(25)));
    }

    @Test
    void rejectsNull() {
        assertThatNullPointerException().isThrownBy(() -> new Nickname(null));
    }
}
