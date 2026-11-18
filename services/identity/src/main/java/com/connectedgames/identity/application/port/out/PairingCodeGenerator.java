package com.connectedgames.identity.application.port.out;

/** Draws the 6 characters of a pairing code: unpredictable, not a sequence */
public interface PairingCodeGenerator {

    String generate();
}
