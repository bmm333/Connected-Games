package com.connectedgames.match.domain.event;

import com.connectedgames.match.Fixtures;
import com.connectedgames.match.domain.TenantId;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.connectedgames.match.domain.EventId;
import com.connectedgames.match.domain.GameMode;
import com.connectedgames.match.domain.MatchId;
import com.connectedgames.match.domain.TableId;
import com.connectedgames.match.domain.VenueId;

import static com.connectedgames.match.domain.Side.BLUE;
import static com.connectedgames.match.domain.Side.RED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class EventsTest {

    private static final EventId ID = new EventId(UUID.randomUUID());
    private static final VenueId VENUE = new VenueId(UUID.randomUUID());
    private static final TableId TABLE = new TableId(UUID.randomUUID());
    private static final MatchId MATCH = new MatchId(UUID.randomUUID());
    private static final Instant NOW = Instant.parse("2026-11-15T20:00:00Z");

    private static final List<SideSetup> BOTH_SIDES = List.of(new SideSetup(RED, null, null), new SideSetup(BLUE, null, null));
    private static final List<SideScore> BOTH_SCORES = List.of(new SideScore(RED, 1), new SideScore(BLUE, 0));

    @Test
    void seqStartsAtOne() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new GoalScored(ID, VENUE, TABLE, MATCH, 0, NOW, RED));
    }

    @Test
    void aGoalNeedsItsSide() {
        assertThatNullPointerException()
                .isThrownBy(() -> new GoalScored(ID, VENUE, TABLE, MATCH, 1, NOW, null));
    }

    @Test
    void aStartNeedsAtLeastOnePointToWin() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new MatchStarted(ID, VENUE, TABLE, MATCH, 1, NOW, Fixtures.TENANT, Fixtures.GAME_TYPE, null, GameMode.INDIVIDUAL, 0, BOTH_SIDES));
    }

    @Test
    void aStartNeedsATenantAndAGameType() {
        assertThatNullPointerException().isThrownBy(() -> new MatchStarted(ID, VENUE, TABLE, MATCH, 1, NOW,
                null, Fixtures.GAME_TYPE, null, GameMode.INDIVIDUAL, 5, BOTH_SIDES));
        assertThatNullPointerException().isThrownBy(() -> new MatchStarted(ID, VENUE, TABLE, MATCH, 1, NOW,
                Fixtures.TENANT, null, null, GameMode.INDIVIDUAL, 5, BOTH_SIDES));
    }

    @Test
    void aStartMayHaveNoTournamentMatch() {
        MatchStarted friendly = new MatchStarted(ID, VENUE, TABLE, MATCH, 1, NOW,
                Fixtures.TENANT, Fixtures.GAME_TYPE, null, GameMode.INDIVIDUAL, 5, BOTH_SIDES);

        assertThat(friendly.tournamentMatchId()).isNull();
    }

    @Test
    void aTenantIdCannotBeBlank() {
        assertThatIllegalArgumentException().isThrownBy(() -> new TenantId(" "));
        assertThatNullPointerException().isThrownBy(() -> new TenantId(null));
    }

    @Test
    void aStartNeedsBothSides() {
        List<SideSetup> twoReds = List.of(new SideSetup(RED, null, null), new SideSetup(RED, null, null));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> new MatchStarted(ID, VENUE, TABLE, MATCH, 1, NOW, Fixtures.TENANT, Fixtures.GAME_TYPE, null, GameMode.INDIVIDUAL, 5, twoReds));
    }

    @Test
    void aStartKeepsItsOwnCopyOfTheSides() {
        List<SideSetup> sides = new ArrayList<>(BOTH_SIDES);
        MatchStarted started = new MatchStarted(ID, VENUE, TABLE, MATCH, 1, NOW, Fixtures.TENANT, Fixtures.GAME_TYPE, null, GameMode.INDIVIDUAL, 5, sides);

        sides.clear();

        assertThat(started.sides()).hasSize(2);
    }

    @Test
    void anEndNeedsBothScores() {
        List<SideScore> onlyRed = List.of(new SideScore(RED, 1));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> new MatchEnded(ID, VENUE, TABLE, MATCH, 2, NOW, EndReason.SCORE_REACHED, onlyRed, RED));
    }

    @Test
    void anEndMayHaveNoWinner() {
        MatchEnded ended = new MatchEnded(ID, VENUE, TABLE, MATCH, 2, NOW, EndReason.CANCELLED, BOTH_SCORES, null);

        assertThat(ended.winnerSide()).isNull();
    }

    @Test
    void aScoreCannotBeNegative() {
        assertThatIllegalArgumentException().isThrownBy(() -> new SideScore(RED, -1));
    }

    @Test
    void aStartWithMoreThanTwoSidesIsRejected() {
        List<SideSetup> three = List.of(new SideSetup(RED, null, null), new SideSetup(BLUE, null, null),
                new SideSetup(RED, null, null));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> new MatchStarted(ID, VENUE, TABLE, MATCH, 1, NOW, Fixtures.TENANT, Fixtures.GAME_TYPE, null, GameMode.INDIVIDUAL, 5, three));
    }

    @Test
    void anEndWithMoreThanTwoScoresIsRejected() {
        List<SideScore> three = List.of(new SideScore(RED, 1), new SideScore(BLUE, 0), new SideScore(RED, 2));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> new MatchEnded(ID, VENUE, TABLE, MATCH, 2, NOW, EndReason.SCORE_REACHED, three, RED));
    }
}
