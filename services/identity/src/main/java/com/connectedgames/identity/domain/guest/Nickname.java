package com.connectedgames.identity.domain.guest;

import java.util.Objects;

/** Name chosen by a guest after scanning the table QR code */
public record Nickname(String value) {
    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 24;

    public Nickname{
        Objects.requireNonNull(value,"value");
        value = value.strip();
        if(value.length() < MIN_LENGTH || value.length() > MAX_LENGTH)
            throw new IllegalArgumentException("Nickname must be 2-24 characters: " + value);
    }
}
