package com.connectedgames.tournament.domain.bracket;

import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import com.connectedgames.tournament.domain.MatchId;
import com.connectedgames.tournament.domain.ParticipantId;
import com.connectedgames.tournament.domain.ResultSource;
import com.connectedgames.tournament.domain.Side;
import com.connectedgames.tournament.domain.TournamentMatchId;
import com.connectedgames.tournament.domain.format.SingleElimination;
import com.connectedgames.tournament.domain.match.Entrant;
import com.connectedgames.tournament.domain.match.Outcome;
import com.connectedgames.tournament.domain.match.TournamentMatch;
import com.connectedgames.tournament.domain.match.TournamentMatchStatus;

public final class BracketFixtures {

    public static final Instant NOW = Instant.parse("2026-11-27T10:00:00Z");

    private BracketFixtures() {
    }

    public static List<Entrant> entrants(int count) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(i -> new Entrant(new ParticipantId("p" + i), "Player " + i)).toList();
    }

    public static Bracket bracket(int count, boolean thirdPlace) {
        AtomicLong next = new AtomicLong();
        return new Bracket(new SingleElimination(thirdPlace).createMatches(entrants(count), NOW,
                () -> new TournamentMatchId(new UUID(0, next.incrementAndGet()))));
    }

    public static TournamentMatch matchAt(Bracket bracket, int round, int slot) {
        return bracket.matches().stream()
                .filter(match -> match.round() == round && match.slot() == slot && !match.thirdPlace())
                .findFirst().orElseThrow();
    }

    public static TournamentMatch thirdPlace(Bracket bracket) {
        return bracket.thirdPlaceMatch().orElseThrow();
    }

    public static Outcome manual(Side winner, int red, int blue) {
        return new Outcome(winner, red, blue, ResultSource.MANUAL, null, NOW);
    }

    public static Outcome fromMatch(Side winner, int red, int blue) {
        return new Outcome(winner, red, blue, ResultSource.MATCH, new MatchId(UUID.randomUUID()), NOW);
    }

    public static Bracket play(Bracket bracket, long seed) {
        Random random = new Random(seed);
        Bracket current = bracket;
        while (true) {
            TournamentMatch ready = current.matches().stream()
                    .filter(match -> match.status() == TournamentMatchStatus.READY)
                    .findFirst().orElse(null);
            if (ready == null) {
                return current;
            }
            Side winner = random.nextBoolean() ? Side.RED : Side.BLUE;
            int winnerScore = 5 + random.nextInt(3);
            int loserScore = random.nextInt(5);
            int red = winner == Side.RED ? winnerScore : loserScore;
            int blue = winner == Side.RED ? loserScore : winnerScore;
            current = current.recordResult(ready.id(), fromMatch(winner, red, blue));
        }
    }
}
