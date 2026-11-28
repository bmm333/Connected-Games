package com.connectedgames.tournament.domain.registration;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.connectedgames.tournament.domain.PlayerId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class MemberTest {

    private static final PlayerId PLAYER = new PlayerId(UUID.randomUUID());

    @Test
    void aMemberKeepsThePlayerAndTheName() {
        Member member = new Member(PLAYER, "Ada");

        assertThat(member.playerId()).isEqualTo(PLAYER);
        assertThat(member.displayName()).isEqualTo("Ada");
    }

    @Test
    void theNameHasFromOneToSixtyCharacters() {
        assertThat(new Member(PLAYER, "a").displayName()).isEqualTo("a");
        assertThat(new Member(PLAYER, "a".repeat(60)).displayName()).hasSize(60);
        assertThatIllegalArgumentException().isThrownBy(() -> new Member(PLAYER, ""));
        assertThatIllegalArgumentException().isThrownBy(() -> new Member(PLAYER, "   "));
        assertThatIllegalArgumentException().isThrownBy(() -> new Member(PLAYER, "a".repeat(61)));
    }

    @Test
    void thePlayerAndTheNameAreRequired() {
        assertThatNullPointerException().isThrownBy(() -> new Member(null, "Ada"));
        assertThatNullPointerException().isThrownBy(() -> new Member(PLAYER, null));
    }
}
