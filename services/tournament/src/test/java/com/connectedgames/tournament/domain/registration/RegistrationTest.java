package com.connectedgames.tournament.domain.registration;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.connectedgames.tournament.domain.ParticipantId;
import com.connectedgames.tournament.domain.PlayerId;
import com.connectedgames.tournament.domain.RegistrationId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class RegistrationTest {

    private static final Instant NOW = Instant.parse("2026-11-28T10:00:00Z");
    private static final RegistrationId ID = new RegistrationId(UUID.randomUUID());
    private static final ParticipantId PARTICIPANT = new ParticipantId("squad-1");
    private static final Member ADA = new Member(new PlayerId(UUID.randomUUID()), "Ada");
    private static final Member BRUNO = new Member(new PlayerId(UUID.randomUUID()), "Bruno");

    private static Registration registration(String name, List<Member> members, Integer seed) {
        return new Registration(ID, PARTICIPANT, name, members, seed, NOW);
    }

    @Test
    void aRegistrationKeepsItsValues() {
        Registration registration = registration("Red Devils", List.of(ADA, BRUNO), 3);

        assertThat(registration.id()).isEqualTo(ID);
        assertThat(registration.participantId()).isEqualTo(PARTICIPANT);
        assertThat(registration.displayName()).isEqualTo("Red Devils");
        assertThat(registration.members()).containsExactly(ADA, BRUNO);
        assertThat(registration.seed()).isEqualTo(3);
        assertThat(registration.registeredAt()).isEqualTo(NOW);
    }

    @Test
    void theSeedIsOptional() {
        assertThat(registration("Red Devils", List.of(ADA), null).seed()).isNull();
    }

    @Test
    void theSeedStartsFromOne() {
        assertThatIllegalArgumentException().isThrownBy(() -> registration("Red Devils", List.of(ADA), 0));
        assertThatIllegalArgumentException().isThrownBy(() -> registration("Red Devils", List.of(ADA), -4));
        assertThat(registration("Red Devils", List.of(ADA), 1).seed()).isEqualTo(1);
    }

    @Test
    void theNameHasFromOneToSixtyCharacters() {
        assertThat(registration("a", List.of(ADA), null).displayName()).isEqualTo("a");
        assertThat(registration("a".repeat(60), List.of(ADA), null).displayName()).hasSize(60);
        assertThatIllegalArgumentException().isThrownBy(() -> registration("", List.of(ADA), null));
        assertThatIllegalArgumentException().isThrownBy(() -> registration("  ", List.of(ADA), null));
        assertThatIllegalArgumentException().isThrownBy(() -> registration("a".repeat(61), List.of(ADA), null));
    }

    @Test
    void aRegistrationNeedsAtLeastOneMember() {
        assertThatIllegalArgumentException().isThrownBy(() -> registration("Red Devils", List.of(), null))
                .withMessageContaining("at least one");
    }

    @Test
    void aPlayerCannotBeTwiceInTheSameRegistration() {
        Member adaAgain = new Member(ADA.playerId(), "Ada again");

        assertThatIllegalArgumentException().isThrownBy(() -> registration("Red Devils", List.of(ADA, adaAgain), null))
                .withMessageContaining("twice");
    }

    @Test
    void aRegistrationKeepsItsOwnCopyOfTheMembers() {
        List<Member> members = new ArrayList<>(List.of(ADA));
        Registration registration = registration("Red Devils", members, null);

        members.add(BRUNO);

        assertThat(registration.members()).containsExactly(ADA);
    }

    @Test
    void everythingButTheSeedIsRequired() {
        assertThatNullPointerException().isThrownBy(() -> new Registration(null, PARTICIPANT, "x", List.of(ADA), null, NOW));
        assertThatNullPointerException().isThrownBy(() -> new Registration(ID, null, "x", List.of(ADA), null, NOW));
        assertThatNullPointerException().isThrownBy(() -> new Registration(ID, PARTICIPANT, null, List.of(ADA), null, NOW));
        assertThatNullPointerException().isThrownBy(() -> new Registration(ID, PARTICIPANT, "x", null, null, NOW));
        assertThatNullPointerException().isThrownBy(() -> new Registration(ID, PARTICIPANT, "x", List.of(ADA), null, null));
    }

    @Test
    void withASeedOnlyTheSeedChanges() {
        Registration registration = registration("Red Devils", List.of(ADA, BRUNO), null);

        Registration seeded = registration.withSeed(2);

        assertThat(seeded.seed()).isEqualTo(2);
        assertThat(seeded.id()).isEqualTo(ID);
        assertThat(seeded.participantId()).isEqualTo(PARTICIPANT);
        assertThat(seeded.displayName()).isEqualTo("Red Devils");
        assertThat(seeded.members()).containsExactly(ADA, BRUNO);
        assertThat(seeded.registeredAt()).isEqualTo(NOW);
        assertThat(registration.seed()).isNull();
        assertThat(seeded.withSeed(null).seed()).isNull();
    }

    @Test
    void aSeedBelowOneIsRefusedAlsoWhenSet() {
        assertThatIllegalArgumentException().isThrownBy(() -> registration("x", List.of(ADA), null).withSeed(0));
    }

    @Test
    void itKnowsWhoPlaysInIt() {
        Registration registration = registration("Red Devils", List.of(ADA, BRUNO), null);

        assertThat(registration.hasPlayer(ADA.playerId())).isTrue();
        assertThat(registration.hasPlayer(BRUNO.playerId())).isTrue();
        assertThat(registration.hasPlayer(new PlayerId(UUID.randomUUID()))).isFalse();
    }
}
