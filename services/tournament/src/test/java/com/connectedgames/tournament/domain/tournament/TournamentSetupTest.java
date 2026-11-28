package com.connectedgames.tournament.domain.tournament;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.connectedgames.tournament.domain.GameTypeId;
import com.connectedgames.tournament.domain.standing.ScoringPolicy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class TournamentSetupTest {

    private static final GameTypeId GAME = new GameTypeId(UUID.randomUUID());
    private static final FormatSpec FORMAT = new SingleEliminationSpec(Seeding.RANDOM, false);

    @Test
    void theSetupKeepsItsValues() {
        TournamentSetup setup = new TournamentSetup(GAME, 2, FORMAT, ScoringPolicy.DEFAULT);

        assertThat(setup.gameTypeId()).isEqualTo(GAME);
        assertThat(setup.squadSize()).isEqualTo(2);
        assertThat(setup.format()).isEqualTo(FORMAT);
        assertThat(setup.scoring()).isEqualTo(ScoringPolicy.DEFAULT);
    }

    @Test
    void aSquadOfOneIsAPlayerAndMoreIsASquad() {
        assertThat(new TournamentSetup(GAME, 1, FORMAT, ScoringPolicy.DEFAULT).participantKind())
                .isEqualTo(ParticipantKind.PLAYER);
        assertThat(new TournamentSetup(GAME, 2, FORMAT, ScoringPolicy.DEFAULT).participantKind())
                .isEqualTo(ParticipantKind.SQUAD);
        assertThat(new TournamentSetup(GAME, 5, FORMAT, ScoringPolicy.DEFAULT).participantKind())
                .isEqualTo(ParticipantKind.SQUAD);
    }

    @Test
    void theSquadSizeStartsFromOne() {
        assertThatIllegalArgumentException().isThrownBy(() -> new TournamentSetup(GAME, 0, FORMAT, ScoringPolicy.DEFAULT));
        assertThatIllegalArgumentException().isThrownBy(() -> new TournamentSetup(GAME, -1, FORMAT, ScoringPolicy.DEFAULT));
    }

    @Test
    void everythingIsRequired() {
        assertThatNullPointerException().isThrownBy(() -> new TournamentSetup(null, 1, FORMAT, ScoringPolicy.DEFAULT));
        assertThatNullPointerException().isThrownBy(() -> new TournamentSetup(GAME, 1, null, ScoringPolicy.DEFAULT));
        assertThatNullPointerException().isThrownBy(() -> new TournamentSetup(GAME, 1, FORMAT, null));
    }
}
