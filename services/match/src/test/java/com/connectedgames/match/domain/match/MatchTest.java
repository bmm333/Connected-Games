package com.connectedgames.match.domain.match;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.connectedgames.match.domain.*;
import com.connectedgames.match.domain.event.*;

import static com.connectedgames.match.domain.Side.BLUE;
import static com.connectedgames.match.domain.Side.RED;
import static org.assertj.core.api.Assertions.assertThat;

class MatchTest {

    private static final VenueId VENUE = new VenueId(UUID.randomUUID());
    private static final TableId TABLE = new TableId(UUID.randomUUID());
    private static final MatchId MATCH = new MatchId(UUID.randomUUID());
    private static final Instant NOW = Instant.parse("2026-11-15T20:00:00Z");

    private static EventId newId() { return new EventId(UUID.randomUUID()); }

    private static MatchStarted started(int seq) {
        return new MatchStarted(newId(), VENUE, TABLE, MATCH, seq, NOW, GameMode.INDIVIDUAL, 5,
                List.of(new SideSetup(RED, null, "Red"), new SideSetup(BLUE, null, "Blue")));
    }

    private static GoalScored goal(int seq, Side side) {
        return new GoalScored(newId(), VENUE, TABLE, MATCH, seq, NOW, side);
    }

    private static MatchEnded ended(int seq, int red, int blue) {
        return new MatchEnded(newId(), VENUE, TABLE, MATCH, seq, NOW, EndReason.SCORE_REACHED,
                List.of(new SideScore(RED, red), new SideScore(BLUE, blue)), red > blue ? RED : BLUE);
    }

    @Test
    void theOrderOfArrivalDoesNotMatter() {
        List<MatchEvent> inOrder = List.of(started(1), goal(2, RED), goal(3, BLUE), goal(4, RED));
        List<MatchEvent> shuffled = List.of(inOrder.get(3), inOrder.get(0), inOrder.get(2), inOrder.get(1));

        assertThat(Match.replay(shuffled)).isEqualTo(Match.replay(inOrder));
    }

    @Test
    void withoutTheStartThereIsNoMatchYet() {
        assertThat(Match.replay(List.of(goal(2, RED)))).isEmpty();
    }

    @Test
    void theFinalScoreOfTheEdgeWins() {
        Match match = Match.replay(List.of(started(1), goal(2, RED), ended(9, 5, 3))).orElseThrow();

        assertThat(match.scoreOf(RED)).isEqualTo(5);
        assertThat(match.scoreOf(BLUE)).isEqualTo(3);
    }
}
