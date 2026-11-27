package com.connectedgames.stats.adapters.in.web;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.connectedgames.stats.domain.EndReason;
import com.connectedgames.stats.domain.GameMode;
import com.connectedgames.stats.domain.Side;
import com.connectedgames.stats.domain.result.MatchDetail;
import com.connectedgames.stats.domain.result.MatchResult;
import com.connectedgames.stats.domain.result.Outcome;
import com.connectedgames.stats.domain.result.ScoreAfterPoint;
import com.connectedgames.stats.domain.result.SideResult;
import com.connectedgames.stats.domain.result.Streak;
import com.connectedgames.stats.domain.stats.GameTypeTotals;
import com.connectedgames.stats.domain.stats.LeaderboardEntry;
import com.connectedgames.stats.domain.stats.ParticipantMatch;
import com.connectedgames.stats.domain.stats.ParticipantSummary;
import com.connectedgames.stats.domain.stats.VenueGameTypeTotals;
import com.connectedgames.stats.domain.stats.VenueSummary;
import com.fasterxml.jackson.annotation.JsonInclude;

final class WebResponses {

    private WebResponses() {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record LeaderboardEntryResponse(int rank, String participantId, String displayName, int matches, int wins,
                                    int losses, int draws, int pointsFor, int pointsAgainst) {
        static LeaderboardEntryResponse from(LeaderboardEntry e) {
            return new LeaderboardEntryResponse(e.rank(), e.participantId(), e.displayName(), e.totals().matches(),
                    e.totals().wins(), e.totals().losses(), e.totals().draws(), e.totals().pointsFor(),
                    e.totals().pointsAgainst());
        }
    }

    record GameTypeTotalsResponse(UUID gameTypeId, int matches, int wins, int losses, int draws, int pointsFor,
                                  int pointsAgainst) {
        static GameTypeTotalsResponse from(GameTypeTotals t) {
            return new GameTypeTotalsResponse(t.gameTypeId().value(), t.totals().matches(), t.totals().wins(),
                    t.totals().losses(), t.totals().draws(), t.totals().pointsFor(), t.totals().pointsAgainst());
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record ParticipantSummaryResponse(String participantId, String displayName, Instant lastMatchAt, int matches,
                                      int wins, int losses, int draws, int pointsFor, int pointsAgainst,
                                      List<GameTypeTotalsResponse> byGameType) {
        static ParticipantSummaryResponse from(ParticipantSummary s) {
            return new ParticipantSummaryResponse(s.participantId(), s.displayName(), s.lastMatchAt(),
                    s.totals().matches(), s.totals().wins(), s.totals().losses(), s.totals().draws(),
                    s.totals().pointsFor(), s.totals().pointsAgainst(),
                    s.byGameType().stream().map(GameTypeTotalsResponse::from).toList());
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record ParticipantMatchResponse(UUID matchId, UUID venueId, UUID tableId, UUID gameTypeId,
                                    UUID tournamentMatchId, Instant endedAt, int durationSeconds, Side side,
                                    Outcome result, int pointsFor, int pointsAgainst, String opponentParticipantId,
                                    String opponentDisplayName) {
        static ParticipantMatchResponse from(ParticipantMatch m) {
            return new ParticipantMatchResponse(m.matchId().value(), m.venueId().value(), m.tableId().value(),
                    m.gameTypeId().value(), m.tournamentMatchId() == null ? null : m.tournamentMatchId().value(),
                    m.endedAt(), m.durationSeconds(), m.side(), m.result(), m.pointsFor(), m.pointsAgainst(),
                    m.opponentParticipantId(), m.opponentDisplayName());
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record MatchSideResponse(Side side, String participantId, String displayName, int score) {
        static MatchSideResponse from(SideResult s) {
            return new MatchSideResponse(s.side(), s.participantId(), s.displayName(), s.score());
        }
    }

    record ScoredPointResponse(int seq, Side side, Instant occurredAt, int redScore, int blueScore) {
        static ScoredPointResponse from(ScoreAfterPoint p) {
            return new ScoredPointResponse(p.point().seq(), p.point().side(), p.point().occurredAt(), p.redScore(),
                    p.blueScore());
        }
    }

    record StreakResponse(Side side, int length) {
        static StreakResponse from(Streak s) {
            return s == null ? null : new StreakResponse(s.side(), s.length());
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record MatchResultResponse(UUID matchId, UUID venueId, UUID tableId, UUID gameTypeId, UUID tournamentMatchId,
                               GameMode mode, int pointsToWin, EndReason reason, Side winnerSide, Instant startedAt,
                               Instant endedAt, int durationSeconds, List<MatchSideResponse> sides,
                               List<ScoredPointResponse> points, int leadChanges, StreakResponse longestStreak) {
        static MatchResultResponse from(MatchDetail detail) {
            MatchResult r = detail.result();
            return new MatchResultResponse(r.id().value(), r.venueId().value(), r.tableId().value(),
                    r.gameTypeId().value(), r.tournamentMatchId() == null ? null : r.tournamentMatchId().value(),
                    r.mode(), r.pointsToWin(), r.reason(), r.winnerSide(), r.startedAt(), r.endedAt(),
                    r.durationSeconds(), r.sides().stream().map(MatchSideResponse::from).toList(),
                    detail.timeline().points().stream().map(ScoredPointResponse::from).toList(),
                    detail.timeline().leadChanges(), StreakResponse.from(detail.timeline().longestStreak()));
        }
    }

    record VenueGameTypeResponse(UUID gameTypeId, int matches, int totalPoints, int averageDurationSeconds) {
        static VenueGameTypeResponse from(VenueGameTypeTotals t) {
            return new VenueGameTypeResponse(t.gameTypeId().value(), t.matches(), t.totalPoints(),
                    t.averageDurationSeconds());
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record VenueSummaryResponse(UUID venueId, int matches, int totalPoints, int averageDurationSeconds,
                                Instant lastMatchAt, List<VenueGameTypeResponse> byGameType) {
        static VenueSummaryResponse from(VenueSummary s) {
            return new VenueSummaryResponse(s.venueId().value(), s.matches(), s.totalPoints(),
                    s.averageDurationSeconds(), s.lastMatchAt(),
                    s.byGameType().stream().map(VenueGameTypeResponse::from).toList());
        }
    }
}
