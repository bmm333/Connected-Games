package com.connectedgames.tournament.domain.tournament;

import org.junit.jupiter.api.Test;

import com.connectedgames.tournament.domain.format.SingleElimination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class SingleEliminationSpecTest {

    @Test
    void itBuildsASingleEliminationWithTheThirdPlaceAsAsked() {
        assertThat(new SingleEliminationSpec(Seeding.RANDOM, true).format()).isEqualTo(new SingleElimination(true));
        assertThat(new SingleEliminationSpec(Seeding.RANDOM, false).format()).isEqualTo(new SingleElimination(false));
    }

    @Test
    void itKeepsTheSeeding() {
        assertThat(new SingleEliminationSpec(Seeding.MANUAL, false).seeding()).isEqualTo(Seeding.MANUAL);
        assertThat(new SingleEliminationSpec(Seeding.RANDOM, false).seeding()).isEqualTo(Seeding.RANDOM);
    }

    @Test
    void theSeedingIsRequired() {
        assertThatNullPointerException().isThrownBy(() -> new SingleEliminationSpec(null, false));
    }
}
