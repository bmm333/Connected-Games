package com.connectedgames.tournament.domain.tournament;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.connectedgames.tournament.domain.ParticipantId;
import com.connectedgames.tournament.domain.bracket.Bracket;
import com.connectedgames.tournament.domain.registration.Member;
import com.connectedgames.tournament.domain.registration.Registration;

import static com.connectedgames.tournament.domain.tournament.TournamentFixtures.NOW;
import static com.connectedgames.tournament.domain.tournament.TournamentFixtures.ORGANIZATION;
import static com.connectedgames.tournament.domain.tournament.TournamentFixtures.TOURNAMENT;
import static com.connectedgames.tournament.domain.tournament.TournamentFixtures.details;
import static com.connectedgames.tournament.domain.tournament.TournamentFixtures.openWith;
import static com.connectedgames.tournament.domain.tournament.TournamentFixtures.player;
import static com.connectedgames.tournament.domain.tournament.TournamentFixtures.playAll;
import static com.connectedgames.tournament.domain.tournament.TournamentFixtures.registrationId;
import static com.connectedgames.tournament.domain.tournament.TournamentFixtures.setup;
import static com.connectedgames.tournament.domain.tournament.TournamentFixtures.solo;
import static com.connectedgames.tournament.domain.tournament.TournamentFixtures.squad;
import static com.connectedgames.tournament.domain.tournament.TournamentFixtures.started;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class TournamentInvariantsTest {

    private static final Instant START = NOW.plusSeconds(100);
    private static final Instant END = NOW.plusSeconds(200);

    private static final Bracket UNFINISHED = started(4).bracket();
    private static final Bracket FINISHED = playAll(started(4), END).bracket();

    private static Tournament build(TournamentStatus status, List<Registration> registrations, Bracket bracket,
                                    Instant startedAt, Instant completedAt) {
        return new Tournament(TOURNAMENT, ORGANIZATION, details(), setup(), status, registrations, bracket, NOW,
                startedAt, completedAt);
    }

    private static Tournament buildWith(TournamentDetails details, TournamentSetup setup,
                                        List<Registration> registrations) {
        return new Tournament(TOURNAMENT, ORGANIZATION, details, setup, TournamentStatus.REGISTRATION_OPEN,
                registrations, null, NOW, null, null);
    }

    @Test
    void aValidTournamentIsBuiltInEveryStatus() {
        assertThat(build(TournamentStatus.DRAFT, List.of(), null, null, null).status()).isEqualTo(TournamentStatus.DRAFT);
        assertThat(build(TournamentStatus.REGISTRATION_OPEN, List.of(solo(1)), null, null, null).registeredCount())
                .isEqualTo(1);
        assertThat(build(TournamentStatus.IN_PROGRESS, started(4).registrations(), UNFINISHED, START, null).status())
                .isEqualTo(TournamentStatus.IN_PROGRESS);
        assertThat(build(TournamentStatus.COMPLETED, started(4).registrations(), FINISHED, START, END).status())
                .isEqualTo(TournamentStatus.COMPLETED);
        assertThat(build(TournamentStatus.CANCELLED, List.of(), null, null, null).status())
                .isEqualTo(TournamentStatus.CANCELLED);
        assertThat(build(TournamentStatus.CANCELLED, started(4).registrations(), UNFINISHED, START, null).status())
                .isEqualTo(TournamentStatus.CANCELLED);
    }

    @Test
    void everythingButTheBracketAndTheTimesIsRequired() {
        assertThatNullPointerException().isThrownBy(() -> new Tournament(null, ORGANIZATION, details(), setup(),
                TournamentStatus.DRAFT, List.of(), null, NOW, null, null));
        assertThatNullPointerException().isThrownBy(() -> new Tournament(TOURNAMENT, null, details(), setup(),
                TournamentStatus.DRAFT, List.of(), null, NOW, null, null));
        assertThatNullPointerException().isThrownBy(() -> new Tournament(TOURNAMENT, ORGANIZATION, null, setup(),
                TournamentStatus.DRAFT, List.of(), null, NOW, null, null));
        assertThatNullPointerException().isThrownBy(() -> new Tournament(TOURNAMENT, ORGANIZATION, details(), null,
                TournamentStatus.DRAFT, List.of(), null, NOW, null, null));
        assertThatNullPointerException().isThrownBy(() -> new Tournament(TOURNAMENT, ORGANIZATION, details(), setup(),
                null, List.of(), null, NOW, null, null));
        assertThatNullPointerException().isThrownBy(() -> new Tournament(TOURNAMENT, ORGANIZATION, details(), setup(),
                TournamentStatus.DRAFT, null, null, NOW, null, null));
        assertThatNullPointerException().isThrownBy(() -> new Tournament(TOURNAMENT, ORGANIZATION, details(), setup(),
                TournamentStatus.DRAFT, List.of(), null, null, null, null));
    }

    @Test
    void theRegistrationsCannotBeMoreThanTheMaximum() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> buildWith(details(2, 2), setup(), List.of(solo(1), solo(2), solo(3))))
                .withMessageContaining("maximum");
        assertThat(buildWith(details(2, 2), setup(), List.of(solo(1), solo(2))).registeredCount()).isEqualTo(2);
    }

    @Test
    void aDraftHasNoRegistrations() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> build(TournamentStatus.DRAFT, List.of(solo(1)), null, null, null))
                .withMessageContaining("draft");
    }

    @Test
    void aRegistrationIdCannotBeUsedTwice() {
        Registration sameId = new Registration(registrationId(1), new ParticipantId(player(2).value().toString()),
                "Player 2", List.of(new Member(player(2), "Player 2")), null, NOW);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> buildWith(details(), setup(), List.of(solo(1), sameId)))
                .withMessageContaining("registration is twice");
    }

    @Test
    void aParticipantCannotBeRegisteredTwice() {
        Registration twin = new Registration(registrationId(2), solo(1).participantId(), "Twin",
                List.of(new Member(player(2), "Twin")), null, NOW);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> buildWith(details(), setup(), List.of(solo(1), twin)))
                .withMessageContaining("participant is registered twice");
    }

    @Test
    void aRegistrationHasTheSquadSize() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> buildWith(details(), setup(2, Seeding.RANDOM, false), List.of(squad(1, 3))))
                .withMessageContaining("2 players");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> buildWith(details(), setup(1, Seeding.RANDOM, false), List.of(squad(1, 2))))
                .withMessageContaining("1 players");
        assertThat(buildWith(details(), setup(2, Seeding.RANDOM, false), List.of(squad(1, 2))).registeredCount())
                .isEqualTo(1);
    }

    @Test
    void theParticipantOfAPlayerIsThePlayer() {
        Registration wrong = new Registration(registrationId(1), new ParticipantId("other"), "Player 1",
                List.of(new Member(player(1), "Player 1")), null, NOW);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> buildWith(details(), setup(), List.of(wrong)))
                .withMessageContaining("participant of a player");
    }

    @Test
    void aSquadCanHaveAnyParticipant() {
        assertThat(buildWith(details(), setup(2, Seeding.RANDOM, false), List.of(squad(1, 2))).registrations().get(0)
                .participantId().value()).isEqualTo("squad-1");
    }

    @Test
    void aPlayerCannotBeInTwoRegistrations() {
        Registration overlapping = new Registration(registrationId(2), new ParticipantId("squad-2"), "Overlap",
                List.of(new Member(player(100), "Shared"), new Member(player(800), "Other")), null, NOW);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> buildWith(details(), setup(2, Seeding.RANDOM, false), List.of(squad(1, 2), overlapping)))
                .withMessageContaining("player is registered twice");
    }

    @Test
    void twoRegistrationsCannotShareASeed() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> buildWith(details(), setup(), List.of(solo(1).withSeed(1), solo(2).withSeed(1))))
                .withMessageContaining("same seed");
        assertThat(buildWith(details(), setup(), List.of(solo(1).withSeed(1), solo(2).withSeed(2)))
                .registeredCount()).isEqualTo(2);
        assertThat(buildWith(details(), setup(), List.of(solo(1), solo(2))).registeredCount()).isEqualTo(2);
    }

    @Test
    void aTournamentThatHasNotStartedHasNoBracketAndNoTimes() {
        for (TournamentStatus status : List.of(TournamentStatus.DRAFT, TournamentStatus.REGISTRATION_OPEN)) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> build(status, List.of(), UNFINISHED, null, null))
                    .withMessageContaining("not started");
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> build(status, List.of(), null, START, null))
                    .withMessageContaining("not started");
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> build(status, List.of(), null, null, END))
                    .withMessageContaining("not started");
        }
    }

    @Test
    void aTournamentInProgressHasAnUnfinishedBracketAndNoEnd() {
        List<Registration> registrations = started(4).registrations();

        assertThatIllegalArgumentException()
                .isThrownBy(() -> build(TournamentStatus.IN_PROGRESS, registrations, null, START, null))
                .withMessageContaining("in progress");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> build(TournamentStatus.IN_PROGRESS, registrations, UNFINISHED, null, null))
                .withMessageContaining("in progress");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> build(TournamentStatus.IN_PROGRESS, registrations, UNFINISHED, START, END))
                .withMessageContaining("in progress");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> build(TournamentStatus.IN_PROGRESS, registrations, FINISHED, START, null))
                .withMessageContaining("in progress");
    }

    @Test
    void aCompletedTournamentHasAFinishedBracketAndAnEnd() {
        List<Registration> registrations = started(4).registrations();

        assertThatIllegalArgumentException()
                .isThrownBy(() -> build(TournamentStatus.COMPLETED, registrations, null, START, END))
                .withMessageContaining("completed");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> build(TournamentStatus.COMPLETED, registrations, FINISHED, null, END))
                .withMessageContaining("completed");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> build(TournamentStatus.COMPLETED, registrations, FINISHED, START, null))
                .withMessageContaining("completed");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> build(TournamentStatus.COMPLETED, registrations, UNFINISHED, START, END))
                .withMessageContaining("completed");
    }

    @Test
    void aCancelledTournamentKeepsWhatItHadWhenItStopped() {
        List<Registration> registrations = started(4).registrations();

        assertThatIllegalArgumentException()
                .isThrownBy(() -> build(TournamentStatus.CANCELLED, registrations, UNFINISHED, START, END))
                .withMessageContaining("cancelled");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> build(TournamentStatus.CANCELLED, registrations, UNFINISHED, null, null))
                .withMessageContaining("cancelled");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> build(TournamentStatus.CANCELLED, registrations, null, START, null))
                .withMessageContaining("cancelled");
    }

    @Test
    void theRegistrationsAreCopied() {
        List<Registration> registrations = new ArrayList<>(List.of(solo(1)));
        Tournament tournament = buildWith(details(), setup(), registrations);

        registrations.add(solo(2));

        assertThat(tournament.registrations()).containsExactly(solo(1));
        assertThat(openWith(1).registrations()).hasSize(1);
    }
}
