package com.connectedgames.identity.adapters.out.security;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SecureRandomPairingCodeGeneratorTest {

    private final SecureRandomPairingCodeGenerator generator = new SecureRandomPairingCodeGenerator();

    @Test
    void givesSixUppercaseLettersOrDigits() {
        for (int i = 0; i < 1000; i++) {
            assertThat(generator.generate()).matches("[A-Z0-9]{6}");
        }
    }

    @Test
    void doesNotRepeatItselfInARow() {
        Set<String> codes = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            codes.add(generator.generate());
        }

        // 1000 draws out of 2 billion: a repetition would mean it is not random
        assertThat(codes).hasSize(1000);
    }

    @Test
    void usesTheWholeAlphabet() {
        Set<Character> seen = new HashSet<>();
        for (int i = 0; i < 2000; i++) {
            generator.generate().chars().forEach(c -> seen.add((char) c));
        }

        assertThat(seen).hasSize(36);
    }
}
