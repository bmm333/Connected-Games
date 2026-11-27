package com.connectedgames.stats.adapters.in.web;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.connectedgames.stats.adapters.in.web.WebResponses.LeaderboardEntryResponse;
import com.connectedgames.stats.adapters.in.web.WebResponses.MatchResultResponse;
import com.connectedgames.stats.adapters.in.web.WebResponses.ParticipantMatchResponse;
import com.connectedgames.stats.adapters.in.web.WebResponses.ParticipantSummaryResponse;
import com.connectedgames.stats.adapters.in.web.WebResponses.VenueSummaryResponse;
import com.connectedgames.stats.application.Viewer;
import com.connectedgames.stats.application.port.in.GetLeaderboardUseCase;
import com.connectedgames.stats.application.port.in.GetMatchResultUseCase;
import com.connectedgames.stats.application.port.in.GetParticipantMatchesUseCase;
import com.connectedgames.stats.application.port.in.GetParticipantSummaryUseCase;
import com.connectedgames.stats.application.port.in.GetVenueSummaryUseCase;
import com.connectedgames.stats.domain.GameTypeId;
import com.connectedgames.stats.domain.MatchId;
import com.connectedgames.stats.domain.VenueId;
import com.connectedgames.stats.domain.stats.Page;
import com.connectedgames.stats.domain.stats.StatsFilter;

@RestController
class StatsController {

    private final GetLeaderboardUseCase leaderboard;
    private final GetParticipantSummaryUseCase participantSummary;
    private final GetParticipantMatchesUseCase participantMatches;
    private final GetMatchResultUseCase matchResult;
    private final GetVenueSummaryUseCase venueSummary;

    StatsController(GetLeaderboardUseCase leaderboard, GetParticipantSummaryUseCase participantSummary,
                    GetParticipantMatchesUseCase participantMatches, GetMatchResultUseCase matchResult,
                    GetVenueSummaryUseCase venueSummary) {
        this.leaderboard = leaderboard;
        this.participantSummary = participantSummary;
        this.participantMatches = participantMatches;
        this.matchResult = matchResult;
        this.venueSummary = venueSummary;
    }

    @GetMapping("/leaderboard")
    List<LeaderboardEntryResponse> leaderboard(@AuthenticationPrincipal Jwt token,
            @RequestParam(required = false) UUID venueId, @RequestParam(required = false) UUID gameTypeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "20") int limit) {
        Viewer viewer = ViewerResolver.of(token);
        StatsFilter filter = filter(venueId, gameTypeId, from, to);
        return leaderboard.leaderboard(viewer, filter, page(limit, 0).limit()).stream()
                .map(LeaderboardEntryResponse::from).toList();
    }

    @GetMapping("/participants/{participantId}/summary")
    ParticipantSummaryResponse summary(@AuthenticationPrincipal Jwt token, @PathVariable String participantId,
            @RequestParam(required = false) UUID venueId, @RequestParam(required = false) UUID gameTypeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        Viewer viewer = ViewerResolver.of(token);
        return participantSummary.summary(viewer, participantId, filter(venueId, gameTypeId, from, to))
                .map(ParticipantSummaryResponse::from)
                .orElseThrow(() -> new StatsNotFoundException("Participant " + participantId + " not found"));
    }

    @GetMapping("/participants/{participantId}/matches")
    List<ParticipantMatchResponse> matches(@AuthenticationPrincipal Jwt token, @PathVariable String participantId,
            @RequestParam(required = false) UUID venueId, @RequestParam(required = false) UUID gameTypeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "20") int limit, @RequestParam(defaultValue = "0") int offset) {
        Viewer viewer = ViewerResolver.of(token);
        return participantMatches.matches(viewer, participantId, filter(venueId, gameTypeId, from, to),
                        page(limit, offset))
                .map(list -> list.stream().map(ParticipantMatchResponse::from).toList())
                .orElseThrow(() -> new StatsNotFoundException("Participant " + participantId + " not found"));
    }

    @GetMapping("/matches/{matchId}")
    MatchResultResponse match(@AuthenticationPrincipal Jwt token, @PathVariable UUID matchId) {
        Viewer viewer = ViewerResolver.of(token);
        return matchResult.matchResult(viewer, new MatchId(matchId)).map(MatchResultResponse::from)
                .orElseThrow(() -> new StatsNotFoundException("Match " + matchId + " not found"));
    }

    @GetMapping("/venues/{venueId}/summary")
    VenueSummaryResponse venue(@AuthenticationPrincipal Jwt token, @PathVariable UUID venueId,
            @RequestParam(required = false) UUID gameTypeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        Viewer viewer = ViewerResolver.of(token);
        return venueSummary.venueSummary(viewer, new VenueId(venueId), filter(null, gameTypeId, from, to))
                .map(VenueSummaryResponse::from)
                .orElseThrow(() -> new StatsNotFoundException("Venue " + venueId + " not found"));
    }

    private static StatsFilter filter(UUID venueId, UUID gameTypeId, Instant from, Instant to) {
        try {
            return new StatsFilter(venueId == null ? null : new VenueId(venueId),
                    gameTypeId == null ? null : new GameTypeId(gameTypeId), from, to);
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException(e.getMessage());
        }
    }

    private static Page page(int limit, int offset) {
        try {
            return new Page(limit, offset);
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException(e.getMessage());
        }
    }
}
