package com.connectedgames.catalog.domain.gametype;

import org.junit.jupiter.api.Test;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNoException;

class GameTypeTest {

    private final GameTypeId id = new GameTypeId(UUID.randomUUID());

    @Test
    void unGiocoASquadrePuoAvereDuePartecipantiPerLato() {
        assertThatNoException().isThrownBy(() ->
                new GameType(id, "Calciobalilla", GameMode.TEAM, 2, 10));
    }

    @Test
    void unGiocoIndividualeNonPuoAverePiuDiUnPartecipantePerLato() {
        assertThatIllegalArgumentException().isThrownBy(() ->
                new GameType(id, "Chess", GameMode.INDIVIDUAL, 2, 1));
    }
}
