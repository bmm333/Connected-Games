package com.connectedgames.stats.domain.result;

import com.connectedgames.stats.Fixtures;
import com.connectedgames.stats.domain.Side;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MatchTimelineTest {

    private static MatchTimeline of(String sequence) {
        return MatchTimeline.of(Fixtures.points(sequence));
    }

    @Test
    void a_match_without_points_has_an_empty_timeline() {
        MatchTimeline timeline = MatchTimeline.of(List.of());

        assertThat(timeline.points()).isEmpty();
        assertThat(timeline.leadChanges()).isZero();
        assertThat(timeline.longestStreak()).isNull();
    }

    @Test
    void the_score_after_each_point_is_counted_in_order() {
        MatchTimeline timeline = of("RRBRBRBR");

        assertThat(timeline.points()).extracting(ScoreAfterPoint::redScore)
                .containsExactly(1, 2, 2, 3, 3, 4, 4, 5);
        assertThat(timeline.points()).extracting(ScoreAfterPoint::blueScore)
                .containsExactly(0, 0, 1, 1, 2, 2, 3, 3);
    }

    @Test
    void each_point_keeps_its_own_data() {
        List<ScoredPoint> points = Fixtures.points("RB");

        MatchTimeline timeline = MatchTimeline.of(points);

        assertThat(timeline.points()).extracting(ScoreAfterPoint::point).containsExactlyElementsOf(points);
    }

    @Test
    void the_order_of_the_input_does_not_matter() {
        List<ScoredPoint> shuffled = new ArrayList<>(Fixtures.points("RRBRBRBR"));
        Collections.reverse(shuffled);

        assertThat(MatchTimeline.of(shuffled)).isEqualTo(of("RRBRBRBR"));
    }

    @Test
    void the_first_lead_is_not_a_change() {
        assertThat(of("BBB").leadChanges()).isZero();
        assertThat(of("R").leadChanges()).isZero();
    }

    @Test
    void going_through_a_tie_to_the_other_side_is_a_change() {
        // 1-0 RED, 1-1, 1-2 BLUE
        assertThat(of("RBB").leadChanges()).isEqualTo(1);
    }

    @Test
    void going_back_to_the_same_side_after_a_tie_is_not_a_change() {
        // 1-0 RED, 1-1, 2-1 RED, 2-2
        assertThat(of("RBRB").leadChanges()).isZero();
    }

    @Test
    void every_switch_of_the_leader_counts() {
        // RED, tie, BLUE, tie, RED
        assertThat(of("RBBRR").leadChanges()).isEqualTo(2);
    }

    @Test
    void a_match_that_stays_tied_has_no_lead_to_change() {
        assertThat(of("RBRB").points()).hasSize(4);
        assertThat(of("RB").leadChanges()).isZero();
    }

    @Test
    void the_longest_streak_is_the_most_points_in_a_row() {
        assertThat(of("RBBBRR").longestStreak()).isEqualTo(new Streak(Side.BLUE, 3));
        assertThat(of("BBBRR").longestStreak()).isEqualTo(new Streak(Side.BLUE, 3));
    }

    @Test
    void with_two_streaks_as_long_the_first_one_wins() {
        assertThat(of("RRBB").longestStreak()).isEqualTo(new Streak(Side.RED, 2));
        assertThat(of("BBRRR").longestStreak()).isEqualTo(new Streak(Side.RED, 3));
    }

    @Test
    void a_single_point_is_a_streak_of_one() {
        assertThat(of("B").longestStreak()).isEqualTo(new Streak(Side.BLUE, 1));
        assertThat(of("RB").longestStreak()).isEqualTo(new Streak(Side.RED, 1));
    }

    @Test
    void the_last_streak_is_counted_too() {
        assertThat(of("RBRRRR").longestStreak()).isEqualTo(new Streak(Side.RED, 4));
    }

    @Test
    void a_gap_in_seq_does_not_break_a_streak() {
        // The contract only gives the order of the events, not that seq has no holes
        Instant t = Fixtures.START;
        List<ScoredPoint> points = List.of(new ScoredPoint(2, Side.RED, t), new ScoredPoint(7, Side.RED, t.plusSeconds(1)));

        assertThat(MatchTimeline.of(points).longestStreak()).isEqualTo(new Streak(Side.RED, 2));
    }

    @Test
    void the_timeline_of_the_fixture_match() {
        MatchTimeline timeline = MatchTimeline.of(Fixtures.result().points());

        // RRBRBRBR: 2-0, 2-1, 3-1, 3-2, 4-2, 4-3, 5-3: RED never lost the lead
        assertThat(timeline.leadChanges()).isZero();
        assertThat(timeline.longestStreak()).isEqualTo(new Streak(Side.RED, 2));
        assertThat(timeline.points()).hasSize(8);
    }

    @Test
    void a_streak_needs_at_least_one_point() {
        assertThatThrownBy(() -> new Streak(Side.RED, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Streak(null, 1)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void a_score_cannot_be_negative() {
        ScoredPoint point = Fixtures.points("R").get(0);
        assertThatThrownBy(() -> new ScoreAfterPoint(point, -1, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ScoreAfterPoint(point, 0, -1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ScoreAfterPoint(null, 0, 0)).isInstanceOf(NullPointerException.class);
    }
}
