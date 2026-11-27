package com.connectedgames.stats.domain.result;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.connectedgames.stats.domain.Side;

/**
 * What the points of a match say. The score after each point, how many times lead changed, and longest streak.
 * */

public record MatchTimeline(List<ScoreAfterPoint> points,int leadChanges, Streak longestStreak) {
    public MatchTimeline{
        points=List.copyOf(points);
        if(leadChanges<0){
            throw new IllegalArgumentException("leadChanges cannot be negative");
        }
    }
    public static MatchTimeline of(List<ScoredPoint> points){
        List<ScoreAfterPoint> scores=new ArrayList<>();
        int red=0;
        int blue=0;
        Side lastLeader=null;
        int leadChanges=0;
        Streak longest=null;
        Side streakSide=null;
        int streakLength=0;
        
        for (ScoredPoint point : points.stream().sorted(Comparator.comparingInt(ScoredPoint::seq)).toList()) {
            if (point.side() == Side.RED) {
                red++;
            } else {
                blue++;
            }
            scores.add(new ScoreAfterPoint(point, red, blue));

            Side leader = red > blue ? Side.RED : blue > red ? Side.BLUE : null;
            if (leader != null) {
                if (lastLeader != null && leader != lastLeader) {
                    leadChanges++;
                }
                lastLeader = leader;
            }

            streakLength = point.side() == streakSide ? streakLength + 1 : 1;
            streakSide = point.side();
            if (longest == null || streakLength > longest.length()) {
                longest = new Streak(streakSide, streakLength);
            }
        }
        return new MatchTimeline(scores, leadChanges, longest);
    }
}
