package com.connectedgames.identity.domain.session;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class RefreshTokenTest {

    private static final Instant NOW = Instant.parse("2026-11-03T20:00:00Z");
    private static final String HASH = "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08";
    private static final SessionId SESSION = new SessionId(UUID.randomUUID());

    @Test
    void isIssuedUnused() {
        RefreshToken token = RefreshToken.issue(HASH, SESSION, NOW);

        assertThat(token.issuedAt()).isEqualTo(NOW);
        assertThat(token.usedAt()).isNull();
        assertThat(token.isUsed()).isFalse();
    }

    @Test
    void usingItMarksItAsUsedAndKeepsTheRest() {
        RefreshToken token = RefreshToken.issue(HASH, SESSION, NOW);
        Instant refresh = NOW.plusSeconds(900);

        RefreshToken used = token.used(refresh);

        assertThat(used.isUsed()).isTrue();
        assertThat(used.usedAt()).isEqualTo(refresh);
        assertThat(used.issuedAt()).isEqualTo(NOW);
        assertThat(used.tokenHash()).isEqualTo(HASH);
        assertThat(used.sessionId()).isEqualTo(SESSION);
    }

    @Test
    void usingItDoesNotChangeTheOriginalToken() {
        RefreshToken token = RefreshToken.issue(HASH, SESSION, NOW);

        token.used(NOW.plusSeconds(900));

        assertThat(token.isUsed()).isFalse();
    }

    @Test
    void cannotBeUsedTwice() {
        RefreshToken used = RefreshToken.issue(HASH, SESSION, NOW).used(NOW.plusSeconds(900));

        assertThatIllegalStateException().isThrownBy(() -> used.used(NOW.plusSeconds(1800)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void rejectsBlankHash(String value) {
        assertThatIllegalArgumentException().isThrownBy(() -> RefreshToken.issue(value, SESSION, NOW));
    }

    @Test
    void rejectsNullFields() {
        assertThatNullPointerException().isThrownBy(() -> RefreshToken.issue(null, SESSION, NOW));
        assertThatNullPointerException().isThrownBy(() -> RefreshToken.issue(HASH, null, NOW));
        assertThatNullPointerException().isThrownBy(() -> RefreshToken.issue(HASH, SESSION, null));
    }
}
