package com.connectedgames.tournament.domain.bracket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.connectedgames.tournament.domain.ResultSource;
import com.connectedgames.tournament.domain.TournamentMatchId;
import com.connectedgames.tournament.domain.match.Advance;
import com.connectedgames.tournament.domain.match.Entrant;
import com.connectedgames.tournament.domain.match.Outcome;
import com.connectedgames.tournament.domain.match.TournamentMatch;
import com.connectedgames.tournament.domain.match.TournamentMatchStatus;

public record Bracket(List<TournamentMatch> matches) {
    public Bracket{
        Objects.requireNonNull(matches,"matches");
        matches=List.copyOf(matches);
        if(matches.isEmpty()){
            throw new IllegalArgumentException("A bracket has at least one match");
        }
        Set<TournamentMatchId> ids=new HashSet<>();
        for(TournamentMatch match : matches){
            if(!ids.add(match.id())){
                throw new IllegalArgumentException("A match is in the bracket twice");
            }
            if(match.outcome()!=null && match.outcome().isDraw()){
                throw new IllegalArgumentException("A single elimination has no draws");
            }
        }
        Set<Advance> targets=new HashSet<>();
        for(TournamentMatch match : matches){
            for(Advance advance:advancesOf(match)){
                if (!ids.contains(advance.matchId())) {
                    throw new IllegalArgumentException("A match advances to a match that is not in the bracket");
                }
                if (advance.matchId().equals(match.id())) {
                    throw new IllegalArgumentException("A match cannot advance to itself");
                }
                if (!targets.add(advance)) {
                    throw new IllegalArgumentException("Two matches advance to the same side of a match");
                }
            }
        }
        int last = matches.stream().mapToInt(TournamentMatch::round).max().getAsInt();
        if(matches.stream().filter(match -> match.round() == last && !match.thirdPlace()).count()!=1){
            throw new IllegalArgumentException("A bracket has exactly one final");
        }
        if(matches.stream().filter(TournamentMatch::thirdPlace).count() > 1){
            throw new IllegalArgumentException("A bracket has at most one match for the third place");
        }
    }
    public Optional<TournamentMatch> find(TournamentMatchId id){
        return matches.stream().filter(match -> match.id().equals(id)).findFirst();
    }
    public int rounds() {
        return matches.stream().mapToInt(TournamentMatch::round).max().getAsInt();
    }

    public TournamentMatch finalMatch() {
        return matches.stream()
                .filter(match -> match.round() == rounds() && !match.thirdPlace())
                .findFirst()
                .orElseThrow();
    }

    public Optional<TournamentMatch> thirdPlaceMatch() {
        return matches.stream().filter(TournamentMatch::thirdPlace).findFirst();
    }

    public Entrant champion() {
        return finalMatch().winner();
    }

    public boolean isComplete() {
        return finalMatch().status() == TournamentMatchStatus.COMPLETED
                && thirdPlaceMatch().map(match -> match.status() == TournamentMatchStatus.COMPLETED).orElse(true);
    }

    public Bracket recordResult(TournamentMatchId id, Outcome outcome){
        Objects.requireNonNull(id,"id");
        Objects.requireNonNull(outcome,"outcome");
        TournamentMatch match=find(id).orElseThrow(() -> new UnknownTournamentMatchException(id));
        if(outcome.source()==ResultSource.BYE){
            throw new IllegalArgumentException("A bye is given by the bracket, it cannot be recorded");
        }
        if(outcome.isDraw()){
            throw new IllegalArgumentException("A single elimination does not accept a draw");
        }
        Outcome current = match.outcome();
        if (current == null) {
            if (match.status() != TournamentMatchStatus.READY) {
                throw new ResultNotAllowedException("The match is waiting for its participants");
            }
            return apply(match, outcome);
        }
        if (sameResult(current, outcome)) {
            return this;
        }
        if (current.source() == ResultSource.BYE) {
            throw new ResultNotAllowedException("The participant went through with a bye");
        }
        if (current.source() == ResultSource.MATCH) {
            throw new ResultNotAllowedException("The result of a match cannot be replaced");
        }
        return apply(match, outcome);
    }
    private Bracket apply(TournamentMatch match,Outcome outcome){
        Entrant winner=match.entrantOn(outcome.winnerSide());
        Entrant loser=match.entrantOn(outcome.winnerSide().opposite());
        Map<TournamentMatchId,TournamentMatch> updated=new HashMap<>();
        updated.put(match.id(), match.withOutcome(outcome));
        move(updated, match.winnerGoesTo(), winner);
        move(updated, match.loserGoesTo(), loser);
        List<TournamentMatch> result = new ArrayList<>();
        for (TournamentMatch each : matches) {
            result.add(updated.getOrDefault(each.id(), each));
        }
        return new Bracket(result);
    }
    private void move(Map<TournamentMatchId, TournamentMatch> updated, Advance advance, Entrant entrant) {
        if (advance == null) {
            return;
        }
        TournamentMatch target = updated.getOrDefault(advance.matchId(), find(advance.matchId()).orElseThrow());
        if (entrant.equals(target.entrantOn(advance.side()))) {
            return;
        }
        if (target.outcome() != null) {
            throw new ResultNotAllowedException("The next match already has a result");
        }
        updated.put(target.id(), target.withEntrant(advance.side(), entrant));
    }

    private static boolean sameResult(Outcome a, Outcome b) {
        return a.winnerSide() == b.winnerSide()
                && a.redScore() == b.redScore()
                && a.blueScore() == b.blueScore()
                && a.source() == b.source()
                && Objects.equals(a.matchId(), b.matchId());
    }

    private static List<Advance> advancesOf(TournamentMatch match) {
        List<Advance> advances = new ArrayList<>();
        if (match.winnerGoesTo() != null) {
            advances.add(match.winnerGoesTo());
        }
        if (match.loserGoesTo() != null) {
            advances.add(match.loserGoesTo());
        }
        return advances;
    }
}











