package com.connectedgames.tournament.domain.tournament;

import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.connectedgames.tournament.domain.Side;
import com.connectedgames.tournament.domain.VenueId;

import static com.connectedgames.tournament.domain.tournament.TournamentFixtures.GAME;
import static com.connectedgames.tournament.domain.tournament.TournamentFixtures.NOW;
import static com.connectedgames.tournament.domain.tournament.TournamentFixtures.ORGANIZATION;
import static com.connectedgames.tournament.domain.tournament.TournamentFixtures.TOURNAMENT;
import static com.connectedgames.tournament.domain.tournament.TournamentFixtures.details;
import static com.connectedgames.tournament.domain.tournament.TournamentFixtures.draft;
import static com.connectedgames.tournament.domain.tournament.TournamentFixtures.ids;
import static com.connectedgames.tournament.domain.tournament.TournamentFixtures.open;
import static com.connectedgames.tournament.domain.tournament.TournamentFixtures.openWith;
import static com.connectedgames.tournament.domain.tournament.TournamentFixtures.playAll;
import static com.connectedgames.tournament.domain.tournament.TournamentFixtures.ready;
import static com.connectedgames.tournament.domain.tournament.TournamentFixtures.result;
import static com.connectedgames.tournament.domain.tournament.TournamentFixtures.setup;
import static com.connectedgames.tournament.domain.tournament.TournamentFixtures.solo;
import static com.connectedgames.tournament.domain.tournament.TournamentFixtures.started;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TournamentLifecycleTest {

    static Stream<Arguments> afterRegistrations() {
        return Stream.of(
                Arguments.of("in progress", started(2)),
                Arguments.of("completed", playAll(started(2), NOW)),
                Arguments.of("cancelled", draft().cancel()));
    }

    static Stream<Arguments> everythingButADraft() {
        return Stream.concat(Stream.of(Arguments.of("open", open())), afterRegistrations());
    }

    @Test
    void aDraftStartsWithItsDetailsAndNoOneRegistered() {
        Tournament tournament = draft();

        assertThat(tournament.id()).isEqualTo(TOURNAMENT);
        assertThat(tournament.organizationId()).isEqualTo(ORGANIZATION);
        assertThat(tournament.details()).isEqualTo(details());
        assertThat(tournament.setup()).isEqualTo(setup());
        assertThat(tournament.status()).isEqualTo(TournamentStatus.DRAFT);
        assertThat(tournament.registrations()).isEmpty();
        assertThat(tournament.registeredCount()).isZero();
        assertThat(tournament.bracket()).isNull();
        assertThat(tournament.createdAt()).isEqualTo(NOW);
        assertThat(tournament.startedAt()).isNull();
        assertThat(tournament.completedAt()).isNull();
        assertThat(tournament.standings()).isEmpty();
        assertThat(tournament.currentRound()).isEmpty();
    }

    @Test
    void publishOpensTheRegistrations() {
        Tournament published = draft().publish();

        assertThat(published.status()).isEqualTo(TournamentStatus.REGISTRATION_OPEN);
        assertThat(published.details()).isEqualTo(draft().details());
        assertThat(published.createdAt()).isEqualTo(NOW);
    }

    @Test
    void publishingAnOpenTournamentChangesNothing() {
        Tournament published = draft().publish();

        assertThat(published.publish()).isSameAs(published);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("afterRegistrations")
    void onlyADraftCanBePublished(String state, Tournament tournament) {
        assertThatThrownBy(tournament::publish)
                .isInstanceOf(TournamentConflictException.class)
                .hasMessageContaining("draft");
    }

    @Test
    void aDraftCanBeDeleted() {
        draft().ensureDeletable();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("everythingButADraft")
    void aPublishedTournamentCannotBeDeleted(String state, Tournament tournament) {
        assertThatThrownBy(tournament::ensureDeletable)
                .isInstanceOf(TournamentConflictException.class)
                .hasMessageContaining("cancelled");
    }

    @Test
    void theDetailsCanBeChangedInADraft() {
        VenueId venue = new VenueId(UUID.randomUUID());
        TournamentDetails changed = new TournamentDetails("Spring cup", "Now outside", venue, NOW.plusSeconds(3600),
                NOW.plusSeconds(60), 4, 8);

        Tournament edited = draft().edit(changed);

        assertThat(edited.details()).isEqualTo(changed);
        assertThat(edited.status()).isEqualTo(TournamentStatus.DRAFT);
        assertThat(edited.setup()).isEqualTo(setup());
        assertThat(edited.id()).isEqualTo(TOURNAMENT);
        assertThat(edited.createdAt()).isEqualTo(NOW);
    }

    @Test
    void theDetailsCanBeChangedWhileTheRegistrationsAreOpen() {
        Tournament tournament = openWith(3);

        Tournament edited = tournament.edit(details(2, 10));

        assertThat(edited.details()).isEqualTo(details(2, 10));
        assertThat(edited.status()).isEqualTo(TournamentStatus.REGISTRATION_OPEN);
        assertThat(edited.registrations()).isEqualTo(tournament.registrations());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("afterRegistrations")
    void theDetailsCannotBeChangedOnceStarted(String state, Tournament tournament) {
        assertThatThrownBy(() -> tournament.edit(details(2, 10)))
                .isInstanceOf(TournamentConflictException.class)
                .hasMessageContaining("started");
    }

    @Test
    void theMaximumCannotGoBelowThoseAlreadyRegistered() {
        Tournament tournament = openWith(3);

        assertThatThrownBy(() -> tournament.edit(details(2, 2)))
                .isInstanceOf(TournamentConflictException.class)
                .hasMessageContaining("maximum");
        assertThat(tournament.edit(details(2, 3)).details().maxParticipants()).isEqualTo(3);
    }

    @Test
    void theGameTheFormatAndTheScoringCanBeChangedInADraft() {
        TournamentSetup changed = new TournamentSetup(GAME, 2, new SingleEliminationSpec(Seeding.MANUAL, true),
                setup().scoring());

        Tournament reconfigured = draft().reconfigure(changed);

        assertThat(reconfigured.setup()).isEqualTo(changed);
        assertThat(reconfigured.setup().participantKind()).isEqualTo(ParticipantKind.SQUAD);
        assertThat(reconfigured.details()).isEqualTo(details());
        assertThat(reconfigured.status()).isEqualTo(TournamentStatus.DRAFT);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("everythingButADraft")
    void theGameTheFormatAndTheScoringCannotBeChangedOnceItIsPublished(String state, Tournament tournament) {
        assertThatThrownBy(() -> tournament.reconfigure(setup(1, Seeding.MANUAL, true)))
                .isInstanceOf(TournamentConflictException.class)
                .hasMessageContaining("draft");
    }

    @Test
    void aTournamentCanBeCancelledBeforeAndDuringTheRegistrations() {
        assertThat(draft().cancel().status()).isEqualTo(TournamentStatus.CANCELLED);
        assertThat(open().cancel().status()).isEqualTo(TournamentStatus.CANCELLED);
    }

    @Test
    void cancellingKeepsTheRegistrationsAndTheResults() {
        Tournament tournament = openWith(4).start(NOW.plusSeconds(10), ids(), new Random(1));
        Tournament played = tournament.recordResult(ready(tournament).id(), result(Side.RED, 5, 1), NOW);

        Tournament cancelled = played.cancel();

        assertThat(cancelled.status()).isEqualTo(TournamentStatus.CANCELLED);
        assertThat(cancelled.registrations()).isEqualTo(played.registrations());
        assertThat(cancelled.bracket()).isEqualTo(played.bracket());
        assertThat(cancelled.startedAt()).isEqualTo(played.startedAt());
        assertThat(cancelled.completedAt()).isNull();
        assertThat(cancelled.standings()).isEqualTo(played.standings());
        assertThat(cancelled.currentRound()).isEmpty();
    }

    @Test
    void cancellingACancelledTournamentChangesNothing() {
        Tournament cancelled = open().cancel();

        assertThat(cancelled.cancel()).isSameAs(cancelled);
    }

    @Test
    void aCompletedTournamentCannotBeCancelled() {
        Tournament completed = playAll(started(2), NOW);

        assertThatThrownBy(completed::cancel)
                .isInstanceOf(TournamentConflictException.class)
                .hasMessageContaining("completed");
    }

    @Test
    void aCancelledTournamentCannotGoOn() {
        Tournament cancelled = open().cancel();

        assertThatThrownBy(cancelled::publish).isInstanceOf(TournamentConflictException.class);
        assertThatThrownBy(() -> cancelled.register(solo(1), NOW))
                .isInstanceOf(TournamentConflictException.class);
        assertThatThrownBy(() -> cancelled.start(NOW, ids(), new Random(1)))
                .isInstanceOf(TournamentConflictException.class);
        assertThatThrownBy(() -> cancelled.edit(details(2, 4))).isInstanceOf(TournamentConflictException.class);
    }

    @Test
    void theArgumentsAreRequired() {
        assertThatNullPointerException().isThrownBy(() -> draft().edit(null));
        assertThatNullPointerException().isThrownBy(() -> draft().reconfigure(null));
    }

    @Test
    void theKindOfParticipantFollowsTheSquadSize() {
        assertThat(draft().setup().participantKind()).isEqualTo(ParticipantKind.PLAYER);
        assertThat(draft(details(), setup(3, Seeding.RANDOM, false)).setup().participantKind())
                .isEqualTo(ParticipantKind.SQUAD);
    }
}
