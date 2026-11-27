package com.connectedgames.stats.domain.stats;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.connectedgames.stats.domain.GameTypeId;
import com.connectedgames.stats.domain.VenueId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StatsModelsTest {

    private static final Instant T0 = Instant.parse("2026-11-16T20:00:00Z");

    // ---- Totals

    @Test
    void theDifferenceOfPointsIsPointsForMinusPointsAgainst() {
        assertThat(new Totals(3, 2, 1, 0, 14, 9).pointDifference()).isEqualTo(5);
        assertThat(new Totals(1, 0, 1, 0, 3, 10).pointDifference()).isEqualTo(-7);
        assertThat(Totals.none().pointDifference()).isZero();
    }

    @Test
    void winsLossesAndDrawsMustAddUpToTheMatches() {
        assertThatThrownBy(() -> new Totals(3, 1, 1, 0, 0, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Totals(1, 1, 1, 0, 0, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThat(new Totals(3, 1, 1, 1, 0, 0).matches()).isEqualTo(3);
    }

    @Test
    void aTotalCannotBeNegative() {
        assertThatThrownBy(() -> new Totals(-1, 0, 0, 0, 0, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Totals(1, 1, 0, 0, -1, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Totals(1, 1, 0, 0, 0, -1)).isInstanceOf(IllegalArgumentException.class);
    }

    // ---- StatsFilter

    @Test
    void aFilterCannotStartAfterItEnds() {
        assertThatThrownBy(() -> new StatsFilter(null, null, T0.plusSeconds(1), T0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(new StatsFilter(null, null, T0, T0)).isNotNull();
        assertThat(new StatsFilter(null, null, T0, null)).isNotNull();
        assertThat(StatsFilter.none()).isEqualTo(new StatsFilter(null, null, null, null));
    }

    // ---- Page

    @Test
    void aPageHasALimitBetweenOneAndOneHundredAndAnOffsetFromZero() {
        assertThat(new Page(1, 0).limit()).isEqualTo(1);
        assertThat(new Page(100, 5).offset()).isEqualTo(5);
        assertThat(Page.first()).isEqualTo(new Page(20, 0));
        assertThatThrownBy(() -> new Page(0, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Page(101, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Page(10, -1)).isInstanceOf(IllegalArgumentException.class);
    }

    // ---- LeaderboardEntry

    @Test
    void theRankStartsAtOne() {
        assertThatThrownBy(() -> new LeaderboardEntry(0, "team-1", null, Totals.none()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(new LeaderboardEntry(1, "team-1", null, Totals.none()).displayName()).isNull();
    }

    // ---- ParticipantSummary and VenueSummary

    @Test
    void theGameTypesOfASummaryAddUpToItsMatches() {
        GameTypeId foosball = new GameTypeId(UUID.randomUUID());
        GameTypeId chess = new GameTypeId(UUID.randomUUID());
        List<GameTypeTotals> games = List.of(new GameTypeTotals(foosball, new Totals(2, 1, 1, 0, 8, 7)),
                new GameTypeTotals(chess, new Totals(1, 1, 0, 0, 1, 0)));

        assertThat(new ParticipantSummary("team-1", null, T0, new Totals(3, 2, 1, 0, 9, 7), games).byGameType())
                .hasSize(2);
        assertThatThrownBy(() -> new ParticipantSummary("team-1", null, T0, new Totals(4, 3, 1, 0, 9, 7), games))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void theGameTypesOfAVenueAddUpToItsMatches() {
        VenueId venue = new VenueId(UUID.randomUUID());
        List<VenueGameTypeTotals> games = List.of(new VenueGameTypeTotals(new GameTypeId(UUID.randomUUID()), 3, 20, 300));

        assertThat(new VenueSummary(venue, 3, 20, 300, T0, games).matches()).isEqualTo(3);
        assertThatThrownBy(() -> new VenueSummary(venue, 4, 20, 300, T0, games))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(new VenueSummary(venue, 0, 0, 0, null, List.of()).lastMatchAt()).isNull();
    }
}
