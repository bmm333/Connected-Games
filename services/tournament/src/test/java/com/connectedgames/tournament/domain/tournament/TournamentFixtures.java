package com.connectedgames.tournament.domain.tournament;

import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import com.connectedgames.tournament.domain.GameTypeId;
import com.connectedgames.tournament.domain.MatchId;
import com.connectedgames.tournament.domain.OrganizationId;
import com.connectedgames.tournament.domain.ParticipantId;
import com.connectedgames.tournament.domain.PlayerId;
import com.connectedgames.tournament.domain.RegistrationId;
import com.connectedgames.tournament.domain.ResultSource;
import com.connectedgames.tournament.domain.Side;
import com.connectedgames.tournament.domain.TournamentId;
import com.connectedgames.tournament.domain.TournamentMatchId;
import com.connectedgames.tournament.domain.match.Outcome;
import com.connectedgames.tournament.domain.match.TournamentMatch;
import com.connectedgames.tournament.domain.match.TournamentMatchStatus;
import com.connectedgames.tournament.domain.registration.Member;
import com.connectedgames.tournament.domain.registration.Registration;
import com.connectedgames.tournament.domain.standing.ScoringPolicy;

final class TournamentFixtures {

    static final Instant NOW = Instant.parse("2026-11-28T10:00:00Z");
    static final TournamentId TOURNAMENT = new TournamentId(new UUID(1, 1));
    static final OrganizationId ORGANIZATION = new OrganizationId(new UUID(1, 2));
    static final GameTypeId GAME = new GameTypeId(new UUID(1, 3));

    private TournamentFixtures() {
    }

    static PlayerId player(int index) {
        return new PlayerId(new UUID(2, index));
    }

    static RegistrationId registrationId(int index) {
        return new RegistrationId(new UUID(3, index));
    }

    static Supplier<TournamentMatchId> ids() {
        AtomicLong next = new AtomicLong();
        return () -> new TournamentMatchId(new UUID(4, next.incrementAndGet()));
    }

    static TournamentDetails details() {
        return new TournamentDetails("Winter cup", null, null, null, null, 2, null);
    }

    static TournamentDetails details(int min, Integer max) {
        return new TournamentDetails("Winter cup", null, null, null, null, min, max);
    }

    static TournamentDetails details(Instant registrationClosesAt) {
        return new TournamentDetails("Winter cup", null, null, null, registrationClosesAt, 2, null);
    }

    static TournamentSetup setup() {
        return setup(1, Seeding.RANDOM, false);
    }

    static TournamentSetup setup(int squadSize, Seeding seeding, boolean thirdPlace) {
        return new TournamentSetup(GAME, squadSize, new SingleEliminationSpec(seeding, thirdPlace),
                ScoringPolicy.DEFAULT);
    }

    static Tournament draft() {
        return Tournament.draft(TOURNAMENT, ORGANIZATION, details(), setup(), NOW);
    }

    static Tournament draft(TournamentDetails details, TournamentSetup setup) {
        return Tournament.draft(TOURNAMENT, ORGANIZATION, details, setup, NOW);
    }

    static Registration solo(int index) {
        PlayerId player = player(index);
        return new Registration(registrationId(index), new ParticipantId(player.value().toString()),
                "Player " + index, List.of(new Member(player, "Player " + index)), null, NOW.plusSeconds(index));
    }

    static Registration squad(int index, int size) {
        List<Member> members = IntStream.range(0, size)
                .mapToObj(k -> new Member(player(index * 100 + k), "Member " + index + "-" + k)).toList();
        return new Registration(registrationId(index), new ParticipantId("squad-" + index), "Squad " + index, members,
                null, NOW.plusSeconds(index));
    }

    static Tournament open() {
        return draft().publish();
    }

    static Tournament openWith(TournamentDetails details, TournamentSetup setup, int count) {
        Tournament tournament = draft(details, setup).publish();
        for (int i = 1; i <= count; i++) {
            tournament = tournament.register(setup.squadSize() == 1 ? solo(i) : squad(i, setup.squadSize()), NOW);
        }
        return tournament;
    }

    static Tournament openWith(int count) {
        return openWith(details(), setup(), count);
    }

    static Tournament started(int count, Seeding seeding, boolean thirdPlace) {
        return openWith(details(), setup(1, seeding, thirdPlace), count)
                .start(NOW.plusSeconds(1000), ids(), new Random(1));
    }

    static Tournament started(int count) {
        return started(count, Seeding.MANUAL, false);
    }

    static Outcome result(Side winner, int red, int blue) {
        return new Outcome(winner, red, blue, ResultSource.MATCH, new MatchId(UUID.randomUUID()), NOW);
    }

    static Outcome byHand(Side winner, int red, int blue) {
        return new Outcome(winner, red, blue, ResultSource.MANUAL, null, NOW);
    }

    static TournamentMatch ready(Tournament tournament) {
        return tournament.bracket().matches().stream()
                .filter(match -> match.status() == TournamentMatchStatus.READY)
                .findFirst().orElse(null);
    }

    static Tournament playAll(Tournament tournament, Instant now) {
        Tournament current = tournament;
        Random random = new Random(5);
        TournamentMatch next = ready(current);
        while (next != null) {
            Side winner = random.nextBoolean() ? Side.RED : Side.BLUE;
            current = current.recordResult(next.id(), result(winner, winner == Side.RED ? 5 : 2,
                    winner == Side.RED ? 2 : 5), now);
            next = ready(current);
        }
        return current;
    }

    static TournamentMatch match(Tournament tournament, int round, int slot) {
        return tournament.bracket().matches().stream()
                .filter(match -> match.round() == round && match.slot() == slot && !match.thirdPlace())
                .findFirst().orElseThrow();
    }
}
