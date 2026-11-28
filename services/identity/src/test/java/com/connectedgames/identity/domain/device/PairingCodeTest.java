package com.connectedgames.identity.domain.device;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class PairingCodeTest {

    private static final Instant NOW = Instant.parse("2026-11-18T20:00:00Z");
    private static final UUID VENUE = UUID.randomUUID();
    private static final UUID ORGANIZATION = UUID.randomUUID();
    private static final UUID TABLE = UUID.randomUUID();

    private PairingCode issued() {
        return PairingCode.issue("K7F2QX", VENUE, ORGANIZATION, TABLE, Surface.KIOSK, NOW);
    }

    @Test
    void keepsTheOrganizationOfTheVenue() {
        assertThat(issued().organizationId()).isEqualTo(ORGANIZATION);
        assertThat(issued().used(NOW.plusSeconds(1)).organizationId()).isEqualTo(ORGANIZATION);
    }

    @Test
    void needsAnOrganization() {
        assertThatNullPointerException()
                .isThrownBy(() -> PairingCode.issue("K7F2QX", VENUE, null, TABLE, Surface.KIOSK, NOW));
    }

    @Test
    void isValidForTenMinutes() {
        assertThat(issued().expiresAt()).isEqualTo(NOW.plus(Duration.ofMinutes(10)));
    }

    @Test
    void isUsableUntilItExpires() {
        PairingCode code = issued();

        assertThat(code.isUsable(NOW)).isTrue();
        assertThat(code.isUsable(NOW.plus(Duration.ofMinutes(10)).minusSeconds(1))).isTrue();
        assertThat(code.isUsable(NOW.plus(Duration.ofMinutes(10)))).isFalse();
    }

    @Test
    void canBeUsedOnlyOnce() {
        PairingCode used = issued().used(NOW.plusSeconds(30));

        assertThat(used.usedAt()).isEqualTo(NOW.plusSeconds(30));
        assertThat(used.isUsable(NOW.plusSeconds(31))).isFalse();
        assertThatIllegalStateException().isThrownBy(() -> used.used(NOW.plusSeconds(31)));
    }

    @Test
    void cannotBeUsedAfterItExpired() {
        assertThatIllegalStateException().isThrownBy(() -> issued().used(NOW.plus(Duration.ofMinutes(11))));
    }

    @ParameterizedTest
    @ValueSource(strings = {"K7F2Q", "K7F2QXX", "k7f2qx", "K7F 2X", "K7F2Q-", ""})
    void refusesWhatIsNotSixUppercaseLettersOrDigits(String value) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> PairingCode.issue(value, VENUE, ORGANIZATION, TABLE, Surface.KIOSK, NOW));
    }
}
