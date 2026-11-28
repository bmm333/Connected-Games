package com.connectedgames.tournament.domain.standing;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public record ScoringPolicy(BigDecimal win, BigDecimal draw, BigDecimal loss, List<Tiebreaker> tiebreakers) {

    public static final ScoringPolicy DEFAULT = new ScoringPolicy(BigDecimal.valueOf(3), BigDecimal.ONE,
            BigDecimal.ZERO, List.of(Tiebreaker.SCORE_DIFFERENCE, Tiebreaker.SCORE_FOR));

    public ScoringPolicy {
        Objects.requireNonNull(win, "win");
        Objects.requireNonNull(draw, "draw");
        Objects.requireNonNull(loss, "loss");
        Objects.requireNonNull(tiebreakers, "tiebreakers");
        if (win.signum() < 0 || draw.signum() < 0 || loss.signum() < 0) {
            throw new IllegalArgumentException("The points of a result cannot be negative");
        }
        tiebreakers = List.copyOf(tiebreakers);
        if (new HashSet<>(tiebreakers).size() != tiebreakers.size()) {
            throw new IllegalArgumentException("A tiebreaker is listed twice");
        }
    }

    public BigDecimal pointsFor(int wins, int draws, int losses) {
        return win.multiply(BigDecimal.valueOf(wins))
                .add(draw.multiply(BigDecimal.valueOf(draws)))
                .add(loss.multiply(BigDecimal.valueOf(losses)));
    }
}
