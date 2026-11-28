package com.connectedgames.tournament.domain.standing;

import java.math.BigDecimal;
import java.util.Objects;

import com.connectedgames.tournament.domain.match.Entrant;

public record Standing(int rank, Entrant entrant, int played, int wins, int draws, int losses, BigDecimal points,
                       int scoreFor, int scoreAgainst, Integer eliminatedInRound) {

    public Standing {
        Objects.requireNonNull(entrant, "entrant");
        Objects.requireNonNull(points, "points");
        if (rank < 1) {
            throw new IllegalArgumentException("rank starts from 1");
        }
        if (wins < 0 || draws < 0 || losses < 0) {
            throw new IllegalArgumentException("A count cannot be negative");
        }
        if (played != wins + draws + losses) {
            throw new IllegalArgumentException("The matches played are the wins, the draws and the losses");
        }
        if (points.signum() < 0) {
            throw new IllegalArgumentException("The points cannot be negative");
        }
        if (scoreFor < 0 || scoreAgainst < 0) {
            throw new IllegalArgumentException("A score cannot be negative");
        }
        if (eliminatedInRound != null && eliminatedInRound < 1) {
            throw new IllegalArgumentException("A round starts from 1");
        }
    }
}
