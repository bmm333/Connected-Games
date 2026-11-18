package com.connectedgames.identity.application;

/** Unknown, expired, already used or for another kind of display: the caller must not learn which */
public class InvalidPairingCodeException extends RuntimeException {

    public InvalidPairingCodeException() {
        super("Pairing code unknown, expired or already used");
    }
}
