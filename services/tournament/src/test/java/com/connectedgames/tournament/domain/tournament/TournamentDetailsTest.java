package com.connectedgames.tournament.domain.tournament;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.connectedgames.tournament.domain.VenueId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class TournamentDetailsTest {

    private static final Instant NOW = Instant.parse("2026-11-28T10:00:00Z");

    private static TournamentDetails named(String name) {
        return new TournamentDetails(name, null, null, null, null, 2, null);
    }

    private static TournamentDetails limits(int min, Integer max) {
        return new TournamentDetails("Cup", null, null, null, null, min, max);
    }

    @Test
    void theDetailsKeepTheirValues() {
        VenueId venue = new VenueId(UUID.randomUUID());

        TournamentDetails details = new TournamentDetails("Winter cup", "For everyone", venue, NOW,
                NOW.minusSeconds(60), 4, 16);

        assertThat(details.name()).isEqualTo("Winter cup");
        assertThat(details.description()).isEqualTo("For everyone");
        assertThat(details.venueId()).isEqualTo(venue);
        assertThat(details.startsAt()).isEqualTo(NOW);
        assertThat(details.registrationClosesAt()).isEqualTo(NOW.minusSeconds(60));
        assertThat(details.minParticipants()).isEqualTo(4);
        assertThat(details.maxParticipants()).isEqualTo(16);
    }

    @Test
    void theOptionalFieldsCanBeMissing() {
        TournamentDetails details = new TournamentDetails("Cup", null, null, null, null, 2, null);

        assertThat(details.description()).isNull();
        assertThat(details.venueId()).isNull();
        assertThat(details.startsAt()).isNull();
        assertThat(details.registrationClosesAt()).isNull();
        assertThat(details.maxParticipants()).isNull();
    }

    @Test
    void theNameHasFromOneToOneHundredTwentyCharacters() {
        assertThat(named("a").name()).isEqualTo("a");
        assertThat(named("a".repeat(120)).name()).hasSize(120);
        assertThatIllegalArgumentException().isThrownBy(() -> named(""));
        assertThatIllegalArgumentException().isThrownBy(() -> named("   "));
        assertThatIllegalArgumentException().isThrownBy(() -> named("a".repeat(121)));
        assertThatNullPointerException().isThrownBy(() -> named(null));
    }

    @Test
    void theDescriptionHasAtMostTwoThousandCharacters() {
        assertThat(new TournamentDetails("Cup", "d".repeat(2000), null, null, null, 2, null).description())
                .hasSize(2000);
        assertThatIllegalArgumentException().isThrownBy(
                () -> new TournamentDetails("Cup", "d".repeat(2001), null, null, null, 2, null));
    }

    @Test
    void aTournamentNeedsAtLeastTwoParticipants() {
        assertThat(limits(2, null).minParticipants()).isEqualTo(2);
        assertThatIllegalArgumentException().isThrownBy(() -> limits(1, null)).withMessageContaining("at least 2");
        assertThatIllegalArgumentException().isThrownBy(() -> limits(0, null));
    }

    @Test
    void theMaximumCannotBeLowerThanTheMinimum() {
        assertThat(limits(4, 4).maxParticipants()).isEqualTo(4);
        assertThatIllegalArgumentException().isThrownBy(() -> limits(4, 3)).withMessageContaining("lower");
    }

    @Test
    void theMaximumIsAtMostOneHundredTwentyEight() {
        assertThat(limits(2, 128).maxParticipants()).isEqualTo(128);
        assertThatIllegalArgumentException().isThrownBy(() -> limits(2, 129)).withMessageContaining("128");
    }

    @Test
    void withoutAMaximumTheCapacityIsOneHundredTwentyEight() {
        assertThat(limits(2, null).capacity()).isEqualTo(128);
        assertThat(limits(2, 16).capacity()).isEqualTo(16);
        assertThat(TournamentDetails.MAX_PARTICIPANTS).isEqualTo(128);
    }
}
