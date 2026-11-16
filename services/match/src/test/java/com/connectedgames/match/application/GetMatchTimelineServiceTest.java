package com.connectedgames.match.application;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.connectedgames.match.domain.EventId;
import com.connectedgames.match.domain.GameMode;
import com.connectedgames.match.domain.MatchId;
import com.connectedgames.match.domain.Side;
import com.connectedgames.match.domain.TableId;
import com.connectedgames.match.domain.VenueId;
import com.connectedgames.match.domain.event.GoalScored;
import com.connectedgames.match.domain.event.MatchEvent;
import com.connectedgames.match.domain.event.MatchStarted;
import com.connectedgames.match.domain.event.SideSetup;
import com.connectedgames.match.domain.match.Match;

import static com.connectedgames.match.domain.Side.BLUE;
import static com.connectedgames.match.domain.Side.RED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetMatchTimelineServiceTest {

    private static final VenueId VENUE = new VenueId(UUID.randomUUID());
    private static final TableId TABLE = new TableId(UUID.randomUUID());
    private static final MatchId MATCH = new MatchId(UUID.randomUUID());
    private static final Instant NOW = Instant.parse("2026-11-15T20:00:00Z");

    private final InMemoryMatchRepository matches = new InMemoryMatchRepository();
    private final InMemoryMatchEventRepository events = new InMemoryMatchEventRepository();
    private final GetMatchTimelineService service = new GetMatchTimelineService(matches, events);

    private static EventId newId() {
        return new EventId(UUID.randomUUID());
    }

    private static MatchStarted started(MatchId match, int seq) {
        return new MatchStarted(newId(), VENUE, TABLE, match, seq, NOW, GameMode.INDIVIDUAL, 5,
                List.of(new SideSetup(RED, null, "Red"), new SideSetup(BLUE, null, "Blue")));
    }

    private static GoalScored goal(MatchId match, int seq, Side side) {
        return new GoalScored(newId(), VENUE, TABLE, match, seq, NOW, side);
    }

    private void record(MatchEvent... toRecord) {
        for (MatchEvent event : toRecord) {
            events.save(event);
        }
        Match.replay(events.findByMatchId(toRecord[0].venueId(), toRecord[0].matchId())).ifPresent(matches::save);
    }

    @Test
    void returnsTheEventsInSequenceOrder() {
        MatchStarted start = started(MATCH, 1);
        GoalScored first = goal(MATCH, 2, RED);
        GoalScored second = goal(MATCH, 3, BLUE);
        record(second, start, first);

        assertThat(service.getMatchTimeline(MATCH)).containsExactly(start, first, second);
    }

    @Test
    void onlyTheEventsOfThatMatch() {
        MatchId other = new MatchId(UUID.randomUUID());
        MatchStarted start = started(MATCH, 1);
        record(start, goal(MATCH, 2, RED));
        record(started(other, 1), goal(other, 2, BLUE));

        assertThat(service.getMatchTimeline(MATCH)).hasSize(2).first().isEqualTo(start);
    }

    @Test
    void anUnknownMatchIsNotFound() {
        assertThatThrownBy(() -> service.getMatchTimeline(new MatchId(UUID.randomUUID())))
                .isInstanceOf(MatchNotFoundException.class);
    }

    @Test
    void eventsWithoutTheStartAreNotATimelineYet() {
        record(goal(MATCH, 2, RED));

        assertThatThrownBy(() -> service.getMatchTimeline(MATCH)).isInstanceOf(MatchNotFoundException.class);
    }

    @Test
    void eventsOfAnotherVenueAreNotInTheTimeline() {
        VenueId other = new VenueId(UUID.randomUUID());
        events.save(new GoalScored(newId(), other, TABLE, MATCH, 2, NOW, BLUE));
        MatchStarted start = started(MATCH, 1);
        record(start);

        assertThat(service.getMatchTimeline(MATCH)).containsExactly(start);
    }
}
