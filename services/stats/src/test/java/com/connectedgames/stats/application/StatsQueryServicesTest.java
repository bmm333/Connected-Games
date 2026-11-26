package com.connectedgames.stats.application;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.connectedgames.stats.Fixtures;
import com.connectedgames.stats.domain.MatchId;
import com.connectedgames.stats.domain.TenantId;
import com.connectedgames.stats.domain.VenueId;
import com.connectedgames.stats.domain.result.MatchDetail;
import com.connectedgames.stats.domain.result.MatchResult;
import com.connectedgames.stats.domain.stats.LeaderboardEntry;
import com.connectedgames.stats.domain.stats.Page;
import com.connectedgames.stats.domain.stats.ParticipantMatch;
import com.connectedgames.stats.domain.stats.ParticipantSummary;
import com.connectedgames.stats.domain.stats.StatsFilter;
import com.connectedgames.stats.domain.stats.Totals;
import com.connectedgames.stats.domain.stats.VenueSummary;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StatsQueryServicesTest {

    private static final TenantId ACME = new TenantId("acme");
    private static final Viewer MANAGER = new Viewer.OfTenants(Set.of(ACME));
    private static final Viewer PLAYER = new Viewer.OfPlayer("player-1");

    private final StubStatsQueries queries = new StubStatsQueries();
    private final StatsFilter filter = StatsFilter.none();

    // ---- leaderboard

    @Test
    void theLeaderboardIsReadInTheTenantsOfTheViewer() {
        LeaderboardEntry entry = new LeaderboardEntry(1, "team-1", "Lightning", new Totals(2, 2, 0, 0, 10, 4));
        queries.leaderboard = List.of(entry);

        List<LeaderboardEntry> result = new GetLeaderboardService(queries).leaderboard(MANAGER, filter, 20);

        assertThat(result).containsExactly(entry);
        assertThat(queries.lastTenants).containsExactly(ACME);
        assertThat(queries.lastFilter).isEqualTo(filter);
        assertThat(queries.lastLimit).isEqualTo(20);
    }

    @Test
    void aPlayerCannotReadTheLeaderboard() {
        assertThatThrownBy(() -> new GetLeaderboardService(queries).leaderboard(PLAYER, filter, 20))
                .isInstanceOf(NotAllowedException.class);
        assertThat(queries.calls).isEmpty();
    }

    @Test
    void theLimitIsBetweenOneAndOneHundred() {
        GetLeaderboardService service = new GetLeaderboardService(queries);

        assertThatThrownBy(() -> service.leaderboard(MANAGER, filter, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.leaderboard(MANAGER, filter, 101)).isInstanceOf(IllegalArgumentException.class);
        service.leaderboard(MANAGER, filter, 100);
        service.leaderboard(MANAGER, filter, 1);
        assertThat(queries.calls).hasSize(2);
    }

    // ---- participant summary

    @Test
    void aManagerReadsTheSummaryOfAnyParticipantOfItsTenants() {
        ParticipantSummary summary = new ParticipantSummary("team-1", "Lightning", null, Totals.none(), List.of());
        queries.participantSummary = summary;

        Optional<ParticipantSummary> result =
                new GetParticipantSummaryService(queries).summary(MANAGER, "team-1", filter);

        assertThat(result).contains(summary);
        assertThat(queries.lastViewer).isEqualTo(MANAGER);
    }

    @Test
    void aPlayerReadsItsOwnSummary() {
        assertThat(new GetParticipantSummaryService(queries).summary(PLAYER, "player-1", filter)).isPresent();
    }

    @Test
    void aPlayerCannotReadTheSummaryOfAnotherParticipant() {
        assertThatThrownBy(() -> new GetParticipantSummaryService(queries).summary(PLAYER, "player-2", filter))
                .isInstanceOf(NotAllowedException.class);
        assertThat(queries.calls).isEmpty();
    }

    @Test
    void anUnknownParticipantHasNoSummary() {
        queries.participantKnown = false;

        assertThat(new GetParticipantSummaryService(queries).summary(MANAGER, "nobody", filter)).isEmpty();
        assertThat(queries.calls).containsExactly("participantKnown");
    }

    // ---- participant matches

    @Test
    void theMatchesOfAParticipantComeWithTheFilterAndThePage() {
        StatsFilter byVenue = new StatsFilter(new VenueId(UUID.randomUUID()), null, null, null);
        Page page = new Page(5, 10);

        Optional<List<ParticipantMatch>> result =
                new GetParticipantMatchesService(queries).matches(MANAGER, "team-1", byVenue, page);

        assertThat(result).contains(List.of());
        assertThat(queries.lastFilter).isEqualTo(byVenue);
        assertThat(queries.lastPage).isEqualTo(page);
    }

    @Test
    void aPlayerCannotReadTheMatchesOfAnotherParticipant() {
        assertThatThrownBy(() -> new GetParticipantMatchesService(queries)
                .matches(PLAYER, "player-2", filter, Page.first())).isInstanceOf(NotAllowedException.class);
        assertThat(queries.calls).isEmpty();
    }

    @Test
    void anUnknownParticipantHasNoMatchesToList() {
        queries.participantKnown = false;

        assertThat(new GetParticipantMatchesService(queries).matches(MANAGER, "nobody", filter, Page.first())).isEmpty();
        assertThat(queries.calls).containsExactly("participantKnown");
    }

    // ---- match result

    @Test
    void aMatchComesWithWhatItsPointsTell() {
        MatchResult match = Fixtures.result();
        queries.matchResult = Optional.of(match);

        Optional<MatchDetail> detail = new GetMatchResultService(queries).matchResult(MANAGER, match.id());

        assertThat(detail).isPresent();
        assertThat(detail.get().result()).isEqualTo(match);
        assertThat(detail.get().timeline().points()).hasSize(8);
        assertThat(detail.get().timeline().longestStreak()).isNotNull();
        assertThat(queries.lastViewer).isEqualTo(MANAGER);
    }

    @Test
    void aMatchTheViewerCannotSeeIsNotFound() {
        assertThat(new GetMatchResultService(queries).matchResult(PLAYER, new MatchId(UUID.randomUUID()))).isEmpty();
        assertThat(queries.lastViewer).isEqualTo(PLAYER);
    }

    // ---- venue summary

    @Test
    void aManagerReadsTheSummaryOfAVenue() {
        VenueId venue = new VenueId(UUID.randomUUID());

        Optional<VenueSummary> result = new GetVenueSummaryService(queries).venueSummary(MANAGER, venue, filter);

        assertThat(result).isPresent();
        assertThat(queries.lastTenants).containsExactly(ACME);
    }

    @Test
    void aPlayerCannotReadTheSummaryOfAVenue() {
        assertThatThrownBy(() -> new GetVenueSummaryService(queries)
                .venueSummary(PLAYER, new VenueId(UUID.randomUUID()), filter)).isInstanceOf(NotAllowedException.class);
        assertThat(queries.calls).isEmpty();
    }

    @Test
    void aVenueWithoutMatchesInTheTenantsIsNotFound() {
        queries.venueKnown = false;

        assertThat(new GetVenueSummaryService(queries).venueSummary(MANAGER, new VenueId(UUID.randomUUID()), filter))
                .isEmpty();
        assertThat(queries.calls).containsExactly("venueKnown");
    }
}
