package com.connectedgames.match.application;

import com.connectedgames.match.Fixtures;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.connectedgames.match.application.port.in.EventTopic;
import com.connectedgames.match.application.port.in.RecordResult;
import com.connectedgames.match.application.port.out.MatchCompleted;
import com.connectedgames.match.domain.EventId;
import com.connectedgames.match.domain.GameMode;
import com.connectedgames.match.domain.MatchId;
import com.connectedgames.match.domain.Side;
import com.connectedgames.match.domain.TableId;
import com.connectedgames.match.domain.VenueId;
import com.connectedgames.match.domain.event.EndReason;
import com.connectedgames.match.domain.event.GoalScored;
import com.connectedgames.match.domain.event.MatchEnded;
import com.connectedgames.match.domain.event.MatchStarted;
import com.connectedgames.match.domain.event.SideScore;
import com.connectedgames.match.domain.event.SideSetup;
import com.connectedgames.match.domain.match.MatchStatus;

import static com.connectedgames.match.domain.Side.BLUE;
import static com.connectedgames.match.domain.Side.RED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class RecordMatchEventServiceTest {

    private static final VenueId VENUE = new VenueId(UUID.randomUUID());
    private static final TableId TABLE = new TableId(UUID.randomUUID());
    private static final MatchId MATCH = new MatchId(UUID.randomUUID());
    private static final Instant NOW = Instant.parse("2026-11-15T20:00:00Z");

    private final InMemoryMatchEventRepository events = new InMemoryMatchEventRepository();
    private final InMemoryMatchRepository matches = new InMemoryMatchRepository();
    private final InMemoryMatchCompletedOutbox outbox = new InMemoryMatchCompletedOutbox();
    private final RecordMatchEventService service =
            new RecordMatchEventService(events, matches, outbox, UUID::randomUUID);

    private static EventId newId() {
        return new EventId(UUID.randomUUID());
    }

    private static EventTopic topic() {
        return new EventTopic(VENUE, TABLE, MATCH);
    }

    private static MatchStarted started(int seq) {
        return new MatchStarted(newId(), VENUE, TABLE, MATCH, seq, NOW, Fixtures.TENANT, Fixtures.GAME_TYPE, null, GameMode.INDIVIDUAL, 5,
                List.of(new SideSetup(RED, null, "Red"), new SideSetup(BLUE, null, "Blue")));
    }

    private static GoalScored goal(int seq, Side side) {
        return new GoalScored(newId(), VENUE, TABLE, MATCH, seq, NOW, side);
    }

    private static MatchEnded ended(int seq, int red, int blue) {
        return new MatchEnded(newId(), VENUE, TABLE, MATCH, seq, NOW, EndReason.SCORE_REACHED,
                List.of(new SideScore(RED, red), new SideScore(BLUE, blue)), red > blue ? RED : BLUE);
    }

    private static void assertRejectedWithAReason(RecordResult result) {
        assertThat(result).isInstanceOfSatisfying(RecordResult.Rejected.class,
                rejected -> assertThat(rejected.reason()).isNotBlank());
    }

    @Test
    void acceptsANewEvent() {
        MatchStarted start = started(1);

        RecordResult result = service.record(start, topic());

        assertThat(result).isInstanceOf(RecordResult.Accepted.class);
        assertThat(result.eventId()).isEqualTo(start.id());
        assertThat(events.count()).isEqualTo(1);
    }

    @Test
    void buildsTheMatchFromTheStart() {
        service.record(started(1), topic());

        assertThat(matches.findById(MATCH)).isPresent();
        assertThat(matches.findById(MATCH).orElseThrow().status()).isEqualTo(MatchStatus.IN_PROGRESS);
        assertThat(matches.findCurrentByTable(TABLE)).isPresent();
    }

    @Test
    void everyGoalUpdatesTheMatch() {
        service.record(started(1), topic());
        service.record(goal(2, RED), topic());
        service.record(goal(3, BLUE), topic());
        service.record(goal(4, RED), topic());

        assertThat(matches.findById(MATCH).orElseThrow().scoreOf(RED)).isEqualTo(2);
        assertThat(matches.findById(MATCH).orElseThrow().scoreOf(BLUE)).isEqualTo(1);
    }

    @Test
    void aRepeatedEventIsADuplicate() {
        MatchStarted start = started(1);
        service.record(start, topic());

        RecordResult again = service.record(start, topic());

        assertThat(again).isInstanceOf(RecordResult.Duplicate.class);
        assertThat(again.eventId()).isEqualTo(start.id());
        assertThat(events.count()).isEqualTo(1);
    }

    @Test
    void aRepeatedGoalIsNotCountedTwice() {
        GoalScored goal = goal(2, RED);
        service.record(started(1), topic());
        service.record(goal, topic());

        service.record(goal, topic());

        assertThat(matches.findById(MATCH).orElseThrow().scoreOf(RED)).isEqualTo(1);
        assertThat(events.count()).isEqualTo(2);
    }

    @Test
    void sameSeqWithAnotherIdIsRejected() {
        service.record(started(1), topic());

        RecordResult result = service.record(goal(1, RED), topic());

        assertRejectedWithAReason(result);
        assertThat(events.count()).isEqualTo(1);
        assertThat(matches.findById(MATCH).orElseThrow().scoreOf(RED)).isZero();
    }

    @Test
    void anEventOfAnotherVenueThanTheTopicIsRejected() {
        EventTopic otherVenue = new EventTopic(new VenueId(UUID.randomUUID()), TABLE, MATCH);

        RecordResult result = service.record(started(1), otherVenue);

        assertRejectedWithAReason(result);
        assertThat(events.count()).isZero();
        assertThat(matches.count()).isZero();
    }

    @Test
    void anEventOfAnotherMatchThanTheTopicIsRejected() {
        EventTopic otherMatch = new EventTopic(VENUE, TABLE, new MatchId(UUID.randomUUID()));

        RecordResult result = service.record(started(1), otherMatch);

        assertRejectedWithAReason(result);
        assertThat(events.count()).isZero();
    }

    @Test
    void anotherVenueIsRejectedOnceTheMatchExists() {
        service.record(started(1), topic());
        VenueId other = new VenueId(UUID.randomUUID());
        GoalScored foreign = new GoalScored(newId(), other, TABLE, MATCH, 2, NOW, RED);

        RecordResult result = service.record(foreign, new EventTopic(other, TABLE, MATCH));

        assertRejectedWithAReason(result);
        assertThat(events.count()).isEqualTo(1);
        assertThat(matches.findById(MATCH).orElseThrow().scoreOf(RED)).isZero();
    }

    @Test
    void anEventBeforeTheStartIsKept() {
        RecordResult result = service.record(goal(2, RED), topic());

        assertThat(result).isInstanceOf(RecordResult.Accepted.class);
        assertThat(events.count()).isEqualTo(1);
        assertThat(matches.count()).isZero();

        service.record(started(1), topic());

        assertThat(matches.findById(MATCH).orElseThrow().scoreOf(RED)).isEqualTo(1);
    }

    @Test
    void theEndWritesTheFinalScore() {
        service.record(started(1), topic());
        service.record(goal(2, RED), topic());

        service.record(ended(3, 5, 3), topic());

        var match = matches.findById(MATCH).orElseThrow();
        assertThat(match.status()).isEqualTo(MatchStatus.COMPLETED);
        assertThat(match.scoreOf(RED)).isEqualTo(5);
        assertThat(match.scoreOf(BLUE)).isEqualTo(3);
        assertThat(matches.findCurrentByTable(TABLE)).isEmpty();
    }

    @Test
    void anotherVenueCannotTakeTheSeqOfAMatch() {
        VenueId other = new VenueId(UUID.randomUUID());
        GoalScored foreign = new GoalScored(newId(), other, TABLE, MATCH, 1, NOW, RED);
        service.record(foreign, new EventTopic(other, TABLE, MATCH));

        RecordResult result = service.record(started(1), topic());

        assertThat(result).isInstanceOf(RecordResult.Accepted.class);
        assertThat(matches.findById(MATCH).orElseThrow().scoreOf(RED)).isZero();
    }

    // ---- the message for stats and tournament

    private static MatchEnded cancelled(int seq) {
        return new MatchEnded(newId(), VENUE, TABLE, MATCH, seq, NOW.plusSeconds(60), EndReason.CANCELLED,
                List.of(new SideScore(RED, 0), new SideScore(BLUE, 0)), null);
    }

    @Test
    void aClosedMatchWritesItsMatchCompletedInTheOutbox() {
        service.record(started(1), topic());
        service.record(goal(2, RED), topic());
        service.record(ended(3, 5, 3), topic());

        assertThat(outbox.messages).hasSize(1);
        MatchCompleted message = outbox.messages.get(0);
        assertThat(message.matchId()).isEqualTo(MATCH);
        assertThat(message.venueId()).isEqualTo(VENUE);
        assertThat(message.tableId()).isEqualTo(TABLE);
        assertThat(message.reason()).isEqualTo(EndReason.SCORE_REACHED);
        assertThat(message.winnerSide()).isEqualTo(RED);
        assertThat(message.sides()).extracting(s -> s.score()).containsExactly(5, 3);
        assertThat(message.startedAt()).isEqualTo(NOW);
    }

    @Test
    void theMessageCarriesTenantGameTypeAndTournamentMatchOfTheStart() {
        service.record(new MatchStarted(newId(), VENUE, TABLE, MATCH, 1, NOW, Fixtures.TENANT, Fixtures.GAME_TYPE,
                Fixtures.TOURNAMENT_MATCH, GameMode.TEAM, 5,
                List.of(new SideSetup(RED, "team-1", "Lightning"), new SideSetup(BLUE, null, "Blue"))), topic());
        service.record(ended(2, 5, 1), topic());

        MatchCompleted message = outbox.messages.get(0);
        assertThat(message.tenantId()).isEqualTo(Fixtures.TENANT);
        assertThat(message.gameTypeId()).isEqualTo(Fixtures.GAME_TYPE);
        assertThat(message.tournamentMatchId()).isEqualTo(Fixtures.TOURNAMENT_MATCH);
        assertThat(message.mode()).isEqualTo(GameMode.TEAM);
        assertThat(message.sides().get(0).participantId()).isEqualTo("team-1");
    }

    @Test
    void aMatchInProgressWritesNothing() {
        service.record(started(1), topic());
        service.record(goal(2, RED), topic());

        assertThat(outbox.messages).isEmpty();
    }

    @Test
    void aCancelledMatchIsAnnouncedToo() {
        service.record(started(1), topic());
        service.record(cancelled(2), topic());

        assertThat(outbox.messages).hasSize(1);
        assertThat(outbox.messages.get(0).reason()).isEqualTo(EndReason.CANCELLED);
        assertThat(outbox.messages.get(0).winnerSide()).isNull();
    }

    @Test
    void aLateEventOfAClosedMatchDoesNotWriteTheMessageAgain() {
        service.record(started(1), topic());
        service.record(ended(5, 5, 3), topic());
        MatchCompleted first = outbox.messages.get(0);

        service.record(goal(3, RED), topic());

        assertThat(outbox.messages).containsExactly(first);
    }

    @Test
    void aDuplicateWritesNothingNew() {
        MatchStarted start = started(1);
        MatchEnded end = ended(2, 5, 3);
        service.record(start, topic());
        service.record(end, topic());

        service.record(end, topic());

        assertThat(outbox.messages).hasSize(1);
    }

    @Test
    void aRejectedEventWritesNothing() {
        service.record(started(1), topic());
        EventTopic other = new EventTopic(VENUE, TABLE, new MatchId(UUID.randomUUID()));

        service.record(ended(2, 5, 3), other);

        assertThat(outbox.messages).isEmpty();
    }

    @Test
    void ifTheEndArrivesBeforeTheStartTheMessageIsWrittenWhenTheStartArrives() {
        service.record(ended(2, 5, 3), topic());
        assertThat(outbox.messages).isEmpty();

        service.record(started(1), topic());

        assertThat(outbox.messages).hasSize(1);
        assertThat(outbox.messages.get(0).matchId()).isEqualTo(MATCH);
    }

    @Test
    void theIdOfTheMessageComesFromTheIdSupplier() {
        UUID fixed = UUID.randomUUID();
        RecordMatchEventService withFixedId = new RecordMatchEventService(events, matches, outbox, () -> fixed);

        withFixedId.record(started(1), topic());
        withFixedId.record(ended(2, 5, 3), topic());

        assertThat(outbox.messages.get(0).eventId().value()).isEqualTo(fixed);
    }

    // ---- the points inside the message

    @Test
    void theMessageCarriesThePointsOfTheMatchInOrder() {
        service.record(started(1), topic());
        service.record(goal(2, RED), topic());
        service.record(goal(3, BLUE), topic());
        service.record(goal(4, RED), topic());
        service.record(ended(5, 2, 1), topic());

        assertThat(outbox.messages.get(0).points()).extracting(p -> p.seq(), p -> p.side())
                .containsExactly(tuple(2, RED), tuple(3, BLUE), tuple(4, RED));
    }

    @Test
    void theGoalsInTheMessageAreInOrderEvenIfTheyArrivedOutOfOrder() {
        service.record(started(1), topic());
        service.record(goal(4, RED), topic());
        service.record(goal(2, RED), topic());
        service.record(goal(3, BLUE), topic());
        service.record(ended(5, 2, 1), topic());

        assertThat(outbox.messages.get(0).points()).extracting(p -> p.seq()).containsExactly(2, 3, 4);
    }

    @Test
    void aMatchCancelledBeforeAnyGoalHasNoPoints() {
        service.record(started(1), topic());
        service.record(cancelled(2), topic());

        assertThat(outbox.messages.get(0).points()).isEmpty();
    }

    @Test
    void aGoalThatArrivesAfterTheMessageWasWrittenDoesNotChangeIt() {
        service.record(started(1), topic());
        service.record(goal(2, RED), topic());
        service.record(ended(5, 1, 0), topic());
        MatchCompleted first = outbox.messages.get(0);

        service.record(goal(3, BLUE), topic());

        assertThat(outbox.messages).containsExactly(first);
        assertThat(outbox.messages.get(0).points()).extracting(p -> p.seq()).containsExactly(2);
    }
}
