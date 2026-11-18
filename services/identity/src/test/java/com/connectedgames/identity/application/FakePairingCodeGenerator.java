package com.connectedgames.identity.application;

import com.connectedgames.identity.application.port.out.PairingCodeGenerator;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/** Gives the codes it was told to, in order, so a test can force two equal ones in a row */
class FakePairingCodeGenerator implements PairingCodeGenerator {

    private final Deque<String> codes = new ArrayDeque<>();
    private int drawn;

    FakePairingCodeGenerator(String... codes) {
        this.codes.addAll(List.of(codes));
    }

    @Override
    public String generate() {
        drawn++;
        return codes.removeFirst();
    }

    int drawn() { return drawn; }
}
