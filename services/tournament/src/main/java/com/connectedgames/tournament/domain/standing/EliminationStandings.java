package com.connectedgames.tournament.domain.standing;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.connectedgames.tournament.domain.ParticipantId;
import com.connectedgames.tournament.domain.ResultSource;
import com.connectedgames.tournament.domain.Side;
import com.connectedgames.tournament.domain.bracket.Bracket;
import com.connectedgames.tournament.domain.match.Entrant;
import com.connectedgames.tournament.domain.match.Outcome;
import com.connectedgames.tournament.domain.match.TournamentMatch;
import com.connectedgames.tournament.domain.match.TournamentMatchStatus;

public final class EliminationStandings {

    private EliminationStandings() {
    }

    public static List<Standing> of(Bracket bracket, ScoringPolicy scoring) {
        Objects.requireNonNull(bracket, "bracket");
        Objects.requireNonNull(scoring, "scoring");
        int rounds = bracket.rounds();
        List<TournamentMatch> ordered = bracket.matches().stream()
                .sorted(Comparator.comparingInt(TournamentMatch::round).thenComparingInt(TournamentMatch::slot))
                .toList();

        Map<ParticipantId, Tally> tallies = new LinkedHashMap<>();
        for (TournamentMatch match : ordered) {
            if (match.round() == 1) {
                register(tallies, match.red());
                register(tallies, match.blue());
            }
        }

        for (TournamentMatch match : ordered) {
            Outcome outcome = match.outcome();
            if (outcome == null || outcome.source() == ResultSource.BYE) {
                continue;
            }
            tallies.get(match.red().id()).add(outcome.redScore(), outcome.blueScore(),
                    outcome.winnerSide() == Side.RED);
            tallies.get(match.blue().id()).add(outcome.blueScore(), outcome.redScore(),
                    outcome.winnerSide() == Side.BLUE);
            if (!match.thirdPlace()) {
                tallies.get(match.loser().id()).eliminatedInRound = match.round();
            }
        }

        bracket.thirdPlaceMatch()
                .filter(match -> match.status() == TournamentMatchStatus.COMPLETED)
                .ifPresent(match -> tallies.get(match.loser().id()).rankOverride = 4);

        return tallies.values().stream()
                .map(tally -> tally.toStanding(rounds, scoring))
                .sorted(Comparator.comparingInt(Standing::rank))
                .toList();
    }

    private static void register(Map<ParticipantId, Tally> tallies, Entrant entrant) {
        if (entrant != null) {
            tallies.computeIfAbsent(entrant.id(), id -> new Tally(entrant));
        }
    }

    private static final class Tally {

        private final Entrant entrant;
        private int wins;
        private int losses;
        private int scoreFor;
        private int scoreAgainst;
        private Integer eliminatedInRound;
        private Integer rankOverride;

        private Tally(Entrant entrant) {
            this.entrant = entrant;
        }

        private void add(int scored, int conceded, boolean won) {
            if (won) {
                wins++;
            } else {
                losses++;
            }
            scoreFor += scored;
            scoreAgainst += conceded;
        }

        private Standing toStanding(int rounds, ScoringPolicy scoring) {
            int rank;
            if (rankOverride != null) {
                rank = rankOverride;
            } else if (eliminatedInRound == null) {
                rank = 1;
            } else {
                rank = (1 << (rounds - eliminatedInRound)) + 1;
            }
            return new Standing(rank, entrant, wins + losses, wins, 0, losses, scoring.pointsFor(wins, 0, losses),
                    scoreFor, scoreAgainst, eliminatedInRound);
        }
    }
}
