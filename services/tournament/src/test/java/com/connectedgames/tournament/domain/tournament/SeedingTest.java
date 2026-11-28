package com.connectedgames.tournament.domain.tournament;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.connectedgames.tournament.domain.registration.Registration;

import static com.connectedgames.tournament.domain.tournament.TournamentFixtures.solo;
import static org.assertj.core.api.Assertions.assertThat;

class SeedingTest {

    private static List<Integer> indexes(List<Registration> ordered, List<Registration> original) {
        return ordered.stream().map(original::indexOf).toList();
    }

    private static List<Registration> six() {
        List<Registration> list = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            list.add(solo(i));
        }
        return list;
    }

    @Test
    void manualPutsTheSeededFirstInTheOrderOfTheirSeed() {
        List<Registration> registrations = six();
        registrations.set(4, registrations.get(4).withSeed(1));
        registrations.set(1, registrations.get(1).withSeed(2));
        registrations.set(5, registrations.get(5).withSeed(7));

        List<Registration> ordered = Seeding.MANUAL.order(registrations, new Random(1));

        assertThat(indexes(ordered, registrations)).containsExactly(4, 1, 5, 0, 2, 3);
    }

    @Test
    void manualKeepsTheOrderOfRegistrationForThoseWithoutASeed() {
        List<Registration> registrations = six();

        List<Registration> ordered = Seeding.MANUAL.order(registrations, new Random(1));

        assertThat(ordered).isEqualTo(registrations);
    }

    @Test
    void manualOrdersByTheValueOfTheSeedAndNotByTheDigits() {
        List<Registration> registrations = six();
        registrations.set(0, registrations.get(0).withSeed(10));
        registrations.set(1, registrations.get(1).withSeed(9));
        registrations.set(2, registrations.get(2).withSeed(100));

        List<Registration> ordered = Seeding.MANUAL.order(registrations, new Random(1));

        assertThat(indexes(ordered, registrations).subList(0, 3)).containsExactly(1, 0, 2);
    }

    @Test
    void manualDoesNotUseTheRandomness() {
        List<Registration> registrations = six();
        registrations.set(3, registrations.get(3).withSeed(1));

        assertThat(Seeding.MANUAL.order(registrations, new Random(1)))
                .isEqualTo(Seeding.MANUAL.order(registrations, new Random(99)));
    }

    @Test
    void manualDoesNotChangeTheListItReceives() {
        List<Registration> registrations = six();
        registrations.set(4, registrations.get(4).withSeed(1));
        List<Registration> before = List.copyOf(registrations);

        Seeding.MANUAL.order(registrations, new Random(1));

        assertThat(registrations).isEqualTo(before);
    }

    @Test
    void randomKeepsEveryone() {
        List<Registration> registrations = six();

        List<Registration> ordered = Seeding.RANDOM.order(registrations, new Random(3));

        assertThat(ordered).hasSize(6).containsExactlyInAnyOrderElementsOf(registrations);
    }

    @Test
    void randomWithTheSameGeneratorGivesTheSameOrder() {
        List<Registration> registrations = six();

        assertThat(Seeding.RANDOM.order(registrations, new Random(8)))
                .isEqualTo(Seeding.RANDOM.order(registrations, new Random(8)));
    }

    @Test
    void randomGivesDifferentOrdersWithDifferentGenerators() {
        List<Registration> registrations = six();
        Set<List<Registration>> orders = new HashSet<>();

        for (long seed = 0; seed < 30; seed++) {
            orders.add(Seeding.RANDOM.order(registrations, new Random(seed)));
        }

        assertThat(orders.size()).isGreaterThan(10);
    }

    @Test
    void randomIgnoresTheSeeds() {
        List<Registration> registrations = six();
        for (int i = 0; i < 6; i++) {
            registrations.set(i, registrations.get(i).withSeed(i + 1));
        }
        Set<Registration> firsts = new HashSet<>();

        for (long seed = 0; seed < 40; seed++) {
            firsts.add(Seeding.RANDOM.order(registrations, new Random(seed)).get(0));
        }

        assertThat(firsts.size()).isGreaterThan(2);
    }

    @Test
    void randomDoesNotChangeTheListItReceives() {
        List<Registration> registrations = six();
        List<Registration> before = List.copyOf(registrations);

        Seeding.RANDOM.order(registrations, new Random(4));

        assertThat(registrations).isEqualTo(before);
    }

    @Test
    void anEmptyListStaysEmpty() {
        assertThat(Seeding.MANUAL.order(List.of(), new Random(1))).isEmpty();
        assertThat(Seeding.RANDOM.order(List.of(), new Random(1))).isEmpty();
    }
}
