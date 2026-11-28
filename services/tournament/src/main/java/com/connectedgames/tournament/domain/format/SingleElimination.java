package com.connectedgames.tournament.domain.format;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import com.connectedgames.tournament.domain.ResultSource;
import com.connectedgames.tournament.domain.Side;
import com.connectedgames.tournament.domain.TournamentMatchId;
import com.connectedgames.tournament.domain.match.Advance;
import com.connectedgames.tournament.domain.match.Entrant;
import com.connectedgames.tournament.domain.match.Outcome;
import com.connectedgames.tournament.domain.match.TournamentMatch;

public record SingleElimination(boolean thirdPlaceMatch) implements TournamentFormat {

    public static final int MAX_ENTRANTS = 128;

    @Override
    public List<TournamentMatch> createMatches(List<Entrant> inSeedOrder, Instant now,
                                               Supplier<TournamentMatchId> ids) {
        Objects.requireNonNull(inSeedOrder, "inSeedOrder");
        Objects.requireNonNull(now, "now");
        Objects.requireNonNull(ids, "ids");
        int count = inSeedOrder.size();
        if (count < 2) {
            throw new IllegalArgumentException("A single elimination needs at least 2 entrants");
        }
        if (count > MAX_ENTRANTS) {
            throw new IllegalArgumentException("A single elimination has at most " + MAX_ENTRANTS + " entrants");
        }
        if (new HashSet<>(inSeedOrder.stream().map(Entrant::id).toList()).size() != count) {
            throw new IllegalArgumentException("A participant is in the bracket twice");
        }

        int size = Integer.highestOneBit(count - 1) << 1;
        int rounds = Integer.numberOfTrailingZeros(size);
        boolean withThirdPlace = thirdPlaceMatch && count >= 4;

        TournamentMatchId[][] matchIds = new TournamentMatchId[rounds][];
        Entrant[][] red = new Entrant[rounds][];
        Entrant[][] blue = new Entrant[rounds][];
        Outcome[][] outcomes = new Outcome[rounds][];
        for (int r = 0; r < rounds; r++) {
            int matches = size >> (r + 1);
            matchIds[r] = new TournamentMatchId[matches];
            red[r] = new Entrant[matches];
            blue[r] = new Entrant[matches];
            outcomes[r] = new Outcome[matches];
            for (int i = 0; i < matches; i++) {
                matchIds[r][i] = ids.get();
            }
        }
        TournamentMatchId thirdPlaceId = withThirdPlace ? ids.get() : null;

        int[] seeds = seedOrder(size);
        for (int i = 0; i < size / 2; i++) {
            red[0][i] = entrantOfSeed(inSeedOrder, seeds[2 * i]);
            blue[0][i] = entrantOfSeed(inSeedOrder, seeds[2 * i + 1]);
            if ((red[0][i] == null) != (blue[0][i] == null)) {
                Side winnerSide = red[0][i] != null ? Side.RED : Side.BLUE;
                outcomes[0][i] = new Outcome(winnerSide, 0, 0, ResultSource.BYE, null, now);
                Entrant winner = red[0][i] != null ? red[0][i] : blue[0][i];
                if (i % 2 == 0) {
                    red[1][i / 2] = winner;
                } else {
                    blue[1][i / 2] = winner;
                }
            }
        }

        List<TournamentMatch> matches = new ArrayList<>();
        for (int r = 0; r < rounds; r++) {
            for (int i = 0; i < matchIds[r].length; i++) {
                Side side = i % 2 == 0 ? Side.RED : Side.BLUE;
                Advance winnerGoesTo = r < rounds - 1 ? new Advance(matchIds[r + 1][i / 2], side) : null;
                Advance loserGoesTo = withThirdPlace && r == rounds - 2 ? new Advance(thirdPlaceId, side) : null;
                matches.add(new TournamentMatch(matchIds[r][i], r + 1, i, false, red[r][i], blue[r][i],
                        winnerGoesTo, loserGoesTo, outcomes[r][i]));
            }
        }
        if (withThirdPlace) {
            matches.add(new TournamentMatch(thirdPlaceId, rounds, 1, true, null, null, null, null, null));
        }
        return List.copyOf(matches);
    }

    private static Entrant entrantOfSeed(List<Entrant> inSeedOrder, int seed) {
        return seed <= inSeedOrder.size() ? inSeedOrder.get(seed - 1) : null;
    }

    private static int[] seedOrder(int size) {
        int[] order = {1};
        for (int length = 1; length < size; length *= 2) {
            int[] next = new int[length * 2];
            for (int i = 0; i < length; i++) {
                next[2 * i] = order[i];
                next[2 * i + 1] = length * 2 + 1 - order[i];
            }
            order = next;
        }
        return order;
    }
}
