package com.connectedgames.tournament.domain.match;

import java.util.Objects;

import com.connectedgames.tournament.domain.ResultSource;
import com.connectedgames.tournament.domain.Side;
import com.connectedgames.tournament.domain.TournamentMatchId;

public record TournamentMatch(TournamentMatchId id, int round, int slot, boolean thirdPlace, Entrant red,
                              Entrant blue, Advance winnerGoesTo, Advance loserGoesTo, Outcome outcome) {

    public TournamentMatch {
        Objects.requireNonNull(id, "id");
        if (round < 1) {
            throw new IllegalArgumentException("round starts from 1");
        }
        if (slot < 0) {
            throw new IllegalArgumentException("slot starts from 0");
        }
        if (red != null && blue != null && red.id().equals(blue.id())) {
            throw new IllegalArgumentException("A participant cannot play against itself");
        }
        if (outcome != null) {
            if (outcome.source() == ResultSource.BYE) {
                if ((red == null) == (blue == null)) {
                    throw new IllegalArgumentException("A bye has exactly one entrant");
                }
            } else if (red == null || blue == null) {
                throw new IllegalArgumentException("A match needs both entrants to have a result");
            }
            Entrant winner = outcome.winnerSide() == Side.RED ? red : blue;
            if (outcome.winnerSide() != null && winner == null) {
                throw new IllegalArgumentException("The winner side has no entrant");
            }
        }
    }

    public TournamentMatchStatus status() {
        if (outcome != null) {
            return TournamentMatchStatus.COMPLETED;
        }
        return red != null && blue != null ? TournamentMatchStatus.READY : TournamentMatchStatus.PENDING;
    }

    public Entrant entrantOn(Side side) {
        return side == Side.RED ? red : blue;
    }

    public Entrant winner() {
        return outcome == null || outcome.winnerSide() == null ? null : entrantOn(outcome.winnerSide());
    }

    public Entrant loser() {
        if (outcome == null || outcome.winnerSide() == null) {
            return null;
        }
        return entrantOn(outcome.winnerSide().opposite());
    }
        public TournamentMatch withEntrant(Side side, Entrant entrant) {
        return new TournamentMatch(id, round, slot, thirdPlace, side == Side.RED ? entrant : red,
                side == Side.BLUE ? entrant : blue, winnerGoesTo, loserGoesTo, outcome);
    }

    public TournamentMatch withOutcome(Outcome newOutcome) {
        return new TournamentMatch(id, round, slot, thirdPlace, red, blue, winnerGoesTo, loserGoesTo, newOutcome);
    }
}
