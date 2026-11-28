package com.connectedgames.tournament.domain.tournament;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.connectedgames.tournament.domain.ParticipantId;
import com.connectedgames.tournament.domain.RegistrationId;
import com.connectedgames.tournament.domain.registration.Member;
import com.connectedgames.tournament.domain.registration.Registration;

import static com.connectedgames.tournament.domain.tournament.TournamentFixtures.NOW;
import static com.connectedgames.tournament.domain.tournament.TournamentFixtures.details;
import static com.connectedgames.tournament.domain.tournament.TournamentFixtures.draft;
import static com.connectedgames.tournament.domain.tournament.TournamentFixtures.open;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TournamentRegistrationTest {

    private static Tournament openUntil(Instant closesAt) {
        return TournamentFixtures.draft(details(closesAt), setup()).publish();
    }

    private static Tournament squads(int count) {
        return openWith(details(), setup(2, Seeding.RANDOM, false), count);
    }

    @Test
    void aPlayerRegistersWhileTheRegistrationsAreOpen() {
        Tournament registered = open().register(solo(1), NOW);

        assertThat(registered.registeredCount()).isEqualTo(1);
        assertThat(registered.registrations()).containsExactly(solo(1));
        assertThat(registered.registration(registrationId(1))).contains(solo(1));
    }

    @Test
    void theRegistrationsStayInTheOrderTheyArrived() {
        Tournament registered = open().register(solo(3), NOW).register(solo(1), NOW).register(solo(2), NOW);

        assertThat(registered.registrations()).containsExactly(solo(3), solo(1), solo(2));
    }

    @Test
    void registeringDoesNotChangeTheOriginal() {
        Tournament tournament = open();

        tournament.register(solo(1), NOW);

        assertThat(tournament.registrations()).isEmpty();
    }

    @Test
    void aRegistrationIsFoundByItsId() {
        Tournament tournament = openWith(3);

        assertThat(tournament.registration(registrationId(2))).contains(solo(2));
        assertThat(tournament.registration(new RegistrationId(UUID.randomUUID()))).isEmpty();
    }

    @Test
    void theRegistrationsAreNotOpenInADraft() {
        assertThatThrownBy(() -> draft().register(solo(1), NOW))
                .isInstanceOf(TournamentConflictException.class)
                .hasMessageContaining("not open");
    }

    @Test
    void theRegistrationsAreNotOpenOnceStarted() {
        assertThatThrownBy(() -> started(2).register(solo(9), NOW))
                .isInstanceOf(TournamentConflictException.class)
                .hasMessageContaining("not open");
        assertThatThrownBy(() -> playAll(started(2), NOW).register(solo(9), NOW))
                .isInstanceOf(TournamentConflictException.class)
                .hasMessageContaining("not open");
    }

    @Test
    void theRegistrationsCloseWhenTheTimeIsPassed() {
        Tournament tournament = openUntil(NOW.plusSeconds(60));

        assertThat(tournament.register(solo(1), NOW.minusSeconds(1)).registeredCount()).isEqualTo(1);
        assertThat(tournament.register(solo(1), NOW.plusSeconds(60)).registeredCount()).isEqualTo(1);
        assertThatThrownBy(() -> tournament.register(solo(1), NOW.plusSeconds(61)))
                .isInstanceOf(TournamentConflictException.class)
                .hasMessageContaining("closed");
    }

    @Test
    void withoutAClosingTimeTheRegistrationsStayOpenUntilItStarts() {
        assertThat(open().register(solo(1), NOW.plusSeconds(100_000_000)).registeredCount()).isEqualTo(1);
    }

    @Test
    void theTournamentCanBeFull() {
        Tournament full = openWith(details(2, 2), setup(), 2);

        assertThatThrownBy(() -> full.register(solo(3), NOW))
                .isInstanceOf(TournamentConflictException.class)
                .hasMessageContaining("full");
    }

    @Test
    void withoutAMaximumTheLimitIsOneHundredTwentyEight() {
        Tournament almost = openWith(128 - 1);
        Tournament full = almost.register(solo(128), NOW);

        assertThat(full.registeredCount()).isEqualTo(128);
        assertThatThrownBy(() -> full.register(solo(129), NOW))
                .isInstanceOf(TournamentConflictException.class)
                .hasMessageContaining("full");
    }

    @Test
    void aPlayerCannotRegisterTwice() {
        Tournament tournament = openWith(2);

        assertThatThrownBy(() -> tournament.register(solo(1), NOW))
                .isInstanceOf(TournamentConflictException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    void aSquadCannotUseAPlayerThatIsAlreadyRegistered() {
        Tournament tournament = squads(2);
        Registration thief = new Registration(registrationId(50), new ParticipantId("squad-50"), "Thieves",
                List.of(new Member(player(100), "Shared"), new Member(player(900), "New")), null, NOW);

        assertThatThrownBy(() -> tournament.register(thief, NOW))
                .isInstanceOf(TournamentConflictException.class)
                .hasMessageContaining("player is already registered");
    }

    @Test
    void aSquadCannotRegisterTwiceWithTheSameParticipant() {
        Tournament tournament = squads(1);
        Registration twin = new Registration(registrationId(60), new ParticipantId("squad-1"), "Twins",
                List.of(new Member(player(700), "A"), new Member(player(701), "B")), null, NOW);

        assertThatThrownBy(() -> tournament.register(twin, NOW))
                .isInstanceOf(TournamentConflictException.class)
                .hasMessageContaining("participant is already registered");
    }

    @Test
    void aSquadRegistersWithExactlyTheSquadSize() {
        Tournament tournament = squads(0);

        assertThat(tournament.register(squad(1, 2), NOW).registeredCount()).isEqualTo(1);
        assertThatIllegalArgumentException().isThrownBy(() -> tournament.register(squad(1, 1), NOW))
                .withMessageContaining("2 players");
        assertThatIllegalArgumentException().isThrownBy(() -> tournament.register(squad(1, 3), NOW))
                .withMessageContaining("2 players");
    }

    @Test
    void aPlayerRegistersWithExactlyOneMember() {
        assertThatIllegalArgumentException().isThrownBy(() -> open().register(squad(1, 2), NOW))
                .withMessageContaining("1 players");
    }

    @Test
    void theParticipantOfAPlayerIsThePlayer() {
        Registration wrong = new Registration(registrationId(1), new ParticipantId("someone-else"), "Player 1",
                List.of(new Member(player(1), "Player 1")), null, NOW);

        assertThatIllegalArgumentException().isThrownBy(() -> open().register(wrong, NOW))
                .withMessageContaining("participant of a player");
    }

    @Test
    void aRegistrationStartsWithoutASeed() {
        assertThatIllegalArgumentException().isThrownBy(() -> open().register(solo(1).withSeed(1), NOW))
                .withMessageContaining("without a seed");
    }

    @Test
    void theArgumentsAreRequired() {
        assertThatNullPointerException().isThrownBy(() -> open().register(null, NOW));
        assertThatNullPointerException().isThrownBy(() -> open().register(solo(1), null));
    }

    @Test
    void aParticipantCanLeaveWhileTheRegistrationsAreOpen() {
        Tournament tournament = openWith(3);

        Tournament left = tournament.withdraw(registrationId(2));

        assertThat(left.registrations()).containsExactly(solo(1), solo(3));
        assertThat(tournament.registeredCount()).isEqualTo(3);
    }

    @Test
    void aParticipantThatLeftCanRegisterAgain() {
        Tournament tournament = openWith(2).withdraw(registrationId(1));

        assertThat(tournament.register(solo(1), NOW).registeredCount()).isEqualTo(2);
    }

    @Test
    void leavingWithAnUnknownRegistrationIsRefused() {
        assertThatThrownBy(() -> openWith(2).withdraw(registrationId(9)))
                .isInstanceOf(UnknownRegistrationException.class)
                .hasMessageContaining(registrationId(9).value().toString());
    }

    @Test
    void aParticipantCannotLeaveOutsideTheRegistrations() {
        assertThatThrownBy(() -> draft().withdraw(registrationId(1)))
                .isInstanceOf(TournamentConflictException.class)
                .hasMessageContaining("leave");
        assertThatThrownBy(() -> started(2).withdraw(registrationId(1)))
                .isInstanceOf(TournamentConflictException.class)
                .hasMessageContaining("leave");
    }

    @Test
    void aSeedIsSetOnARegistration() {
        Tournament seeded = openWith(3).seed(registrationId(2), 1);

        assertThat(seeded.registration(registrationId(2)).orElseThrow().seed()).isEqualTo(1);
        assertThat(seeded.registration(registrationId(1)).orElseThrow().seed()).isNull();
        assertThat(seeded.registration(registrationId(3)).orElseThrow().seed()).isNull();
        assertThat(seeded.registrations().stream().map(Registration::id).toList())
                .containsExactly(registrationId(1), registrationId(2), registrationId(3));
    }

    @Test
    void aSeedCanBeChangedAndCleared() {
        Tournament tournament = openWith(2).seed(registrationId(1), 5);

        assertThat(tournament.seed(registrationId(1), 2).registration(registrationId(1)).orElseThrow().seed())
                .isEqualTo(2);
        assertThat(tournament.seed(registrationId(1), null).registration(registrationId(1)).orElseThrow().seed())
                .isNull();
    }

    @Test
    void theSameSeedOnTheSameRegistrationIsNotAConflict() {
        Tournament tournament = openWith(2).seed(registrationId(1), 5);

        assertThat(tournament.seed(registrationId(1), 5).registration(registrationId(1)).orElseThrow().seed())
                .isEqualTo(5);
    }

    @Test
    void twoRegistrationsCannotHaveTheSameSeed() {
        Tournament tournament = openWith(2).seed(registrationId(1), 5);

        assertThatThrownBy(() -> tournament.seed(registrationId(2), 5))
                .isInstanceOf(TournamentConflictException.class)
                .hasMessageContaining("taken");
    }

    @Test
    void clearingTheSeedOfTwoRegistrationsIsNotAConflict() {
        Tournament tournament = openWith(3).seed(registrationId(1), 5);

        assertThat(tournament.seed(registrationId(2), null).seed(registrationId(3), null).registrations()
                .stream().filter(r -> r.seed() != null).count()).isEqualTo(1);
    }

    @Test
    void aSeedBelowOneIsRefused() {
        assertThatIllegalArgumentException().isThrownBy(() -> openWith(2).seed(registrationId(1), 0));
    }

    @Test
    void aSeedOnAnUnknownRegistrationIsRefused() {
        assertThatThrownBy(() -> openWith(2).seed(registrationId(9), 1))
                .isInstanceOf(UnknownRegistrationException.class);
        assertThatThrownBy(() -> draft().seed(registrationId(1), 1))
                .isInstanceOf(UnknownRegistrationException.class);
    }

    @Test
    void aSeedCannotChangeOnceStarted() {
        assertThatThrownBy(() -> started(2).seed(registrationId(1), 1))
                .isInstanceOf(TournamentConflictException.class)
                .hasMessageContaining("started");
        assertThatThrownBy(() -> playAll(started(2), NOW).seed(registrationId(1), 1))
                .isInstanceOf(TournamentConflictException.class);
        assertThatThrownBy(() -> openWith(2).cancel().seed(registrationId(1), 1))
                .isInstanceOf(TournamentConflictException.class);
    }
}
