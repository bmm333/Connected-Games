package com.connectedgames.match.adapters.in.web;

import com.connectedgames.match.application.MatchNotFoundException;
import com.connectedgames.match.application.port.in.GetCurrentMatchUseCase;
import com.connectedgames.match.application.port.in.GetMatchTimelineUseCase;
import com.connectedgames.match.application.port.in.GetMatchUseCase;
import com.connectedgames.match.domain.EventId;
import com.connectedgames.match.domain.GameMode;
import com.connectedgames.match.domain.MatchId;
import com.connectedgames.match.domain.TableId;
import com.connectedgames.match.domain.VenueId;
import com.connectedgames.match.domain.event.EndReason;
import com.connectedgames.match.domain.event.GoalScored;
import com.connectedgames.match.domain.event.MatchEnded;
import com.connectedgames.match.domain.event.MatchEvent;
import com.connectedgames.match.domain.event.MatchStarted;
import com.connectedgames.match.domain.event.SideScore;
import com.connectedgames.match.domain.event.SideSetup;
import com.connectedgames.match.domain.match.Match;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.connectedgames.match.domain.Side.BLUE;
import static com.connectedgames.match.domain.Side.RED;

/** Use cases with fixed answers: the web tests only check the http answers. */
@TestConfiguration
class StubUseCases {

    static final VenueId VENUE = new VenueId(UUID.randomUUID());
    static final TableId TABLE = new TableId(UUID.randomUUID());
    static final MatchId MATCH = new MatchId(UUID.randomUUID());
    static final MatchId FINISHED = new MatchId(UUID.randomUUID());
    static final Instant START = Instant.parse("2026-11-16T20:00:00Z");

    static final MatchStarted STARTED = new MatchStarted(new EventId(UUID.randomUUID()), VENUE, TABLE, MATCH, 1, START,
            GameMode.TEAM, 5, List.of(new SideSetup(RED, null, "Red"), new SideSetup(BLUE, "team-7", "Lightning")));
    static final GoalScored GOAL = new GoalScored(new EventId(UUID.randomUUID()), VENUE, TABLE, MATCH, 2,
            START.plusSeconds(60), RED);
    static final Match IN_PROGRESS = Match.replay(List.of(STARTED, GOAL)).orElseThrow();

    static final MatchEnded ENDED = new MatchEnded(new EventId(UUID.randomUUID()), VENUE, TABLE, FINISHED, 2,
            START.plusSeconds(600), EndReason.SCORE_REACHED,
            List.of(new SideScore(RED, 5), new SideScore(BLUE, 3)), RED);
    static final Match COMPLETED = Match.replay(List.of(
            new MatchStarted(new EventId(UUID.randomUUID()), VENUE, TABLE, FINISHED, 1, START, GameMode.TEAM, 5,
                    List.of(new SideSetup(RED, null, "Red"), new SideSetup(BLUE, null, "Blue"))),
            ENDED)).orElseThrow();

    @Bean
    GetCurrentMatchUseCase getCurrentMatch() {
        return tableId -> {
            if (tableId.equals(TABLE)) {
                return IN_PROGRESS;
            }
            throw new MatchNotFoundException();
        };
    }

    @Bean
    GetMatchUseCase getMatch() {
        return matchId -> {
            if (matchId.equals(MATCH)) {
                return IN_PROGRESS;
            }
            if (matchId.equals(FINISHED)) {
                return COMPLETED;
            }
            throw new MatchNotFoundException();
        };
    }

    @Bean
    GetMatchTimelineUseCase getMatchTimeline() {
        return matchId -> {
            if (matchId.equals(MATCH)) {
                return List.<MatchEvent>of(STARTED, GOAL);
            }
            throw new MatchNotFoundException();
        };
    }
}
