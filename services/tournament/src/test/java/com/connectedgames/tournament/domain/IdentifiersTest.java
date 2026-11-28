package com.connectedgames.tournament.domain;

import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class IdentifiersTest {

    static Stream<Arguments> identifiers() {
        return Stream.of(
                Arguments.of("TournamentId", (Function<UUID, Object>) TournamentId::new),
                Arguments.of("OrganizationId", (Function<UUID, Object>) OrganizationId::new),
                Arguments.of("VenueId", (Function<UUID, Object>) VenueId::new),
                Arguments.of("GameTypeId", (Function<UUID, Object>) GameTypeId::new),
                Arguments.of("PlayerId", (Function<UUID, Object>) PlayerId::new),
                Arguments.of("RegistrationId", (Function<UUID, Object>) RegistrationId::new));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("identifiers")
    void anIdentifierNeedsAValue(String name, Function<UUID, Object> factory) {
        assertThatNullPointerException().isThrownBy(() -> factory.apply(null));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("identifiers")
    void twoIdentifiersWithTheSameValueAreEqual(String name, Function<UUID, Object> factory) {
        UUID value = UUID.randomUUID();

        assertThat(factory.apply(value)).isEqualTo(factory.apply(value));
        assertThat(factory.apply(value)).isNotEqualTo(factory.apply(UUID.randomUUID()));
    }
}
