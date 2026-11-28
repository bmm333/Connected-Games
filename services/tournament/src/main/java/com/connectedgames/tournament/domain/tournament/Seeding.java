package com.connectedgames.tournament.domain.tournament;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.random.RandomGenerator;

import com.connectedgames.tournament.domain.registration.Registration;

public enum Seeding {

    MANUAL {
        @Override
        public List<Registration> order(List<Registration> registrations, RandomGenerator random) {
            List<Registration> ordered = new ArrayList<>(registrations.stream()
                    .filter(registration -> registration.seed() != null)
                    .sorted(Comparator.comparingInt(Registration::seed))
                    .toList());
            ordered.addAll(registrations.stream().filter(registration -> registration.seed() == null).toList());
            return List.copyOf(ordered);
        }
    },

    RANDOM {
        @Override
        public List<Registration> order(List<Registration> registrations, RandomGenerator random) {
            List<Registration> shuffled = new ArrayList<>(registrations);
            Collections.shuffle(shuffled, random);
            return List.copyOf(shuffled);
        }
    };

    public abstract List<Registration> order(List<Registration> registrations, RandomGenerator random);
}
