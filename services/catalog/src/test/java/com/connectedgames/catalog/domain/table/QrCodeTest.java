package com.connectedgames.catalog.domain.table;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class QrCodeTest {

    @ParameterizedTest
    @ValueSource(strings = {"Ab3_x-9Z", "tavolo2belvedere", "abcdefghijklmnopqrstuvwxyz012345"})
    void acceptsUrlSafeCodesBetween8And32Characters(String code) {
        assertThatNoException().isThrownBy(() -> new QrCode(code));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "abc1234", "abcdefghijklmnopqrstuvwxyz0123456", "with space1", "slash/inside", "accentò123"})
    void rejectsCodesThatAreTooShortTooLongOrNotUrlSafe(String code) {
        assertThatIllegalArgumentException().isThrownBy(() -> new QrCode(code));
    }

    @Test
    void rejectsNull() {
        assertThatNullPointerException().isThrownBy(() -> new QrCode(null));
    }
}
