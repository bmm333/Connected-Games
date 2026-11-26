package com.connectedgames.stats.domain.result;

import java.util.Objects;

public record MatchDetail(MatchResult result, MatchTimeline timeline) {
    public MatchDetail {
        Objects.requireNonNull(result, "result");
        Objects.requireNonNull(timeline, "timeline");
    }

    public static MatchDetail of(MatchResult result) {
        return new MatchDetail(result, MatchTimeline.of(result.points()));
    }
}
