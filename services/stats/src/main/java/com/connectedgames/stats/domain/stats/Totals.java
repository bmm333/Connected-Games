package com.connectedgames.stats.domain.stats;

public record Totals(int matches,int wins,int losses,int draws,int pointsFor,int pointsAgainst) {
    public Totals{
        if(matches < 0 || wins < 0 || losses < 0 || draws < 0 || pointsFor < 0 || pointsAgainst < 0){
            throw new IllegalArgumentException("A total cannot be negative");
        }
        if(wins + losses + draws != matches){
            throw new IllegalArgumentException("Wins, losses and draws must add up to the matches");
        }
    }
    public static Totals none() {
        return new Totals(0,0,0,0,0,0);
    }
    public int pointDifference(){
        return pointsFor - pointsAgainst;
    }
}
