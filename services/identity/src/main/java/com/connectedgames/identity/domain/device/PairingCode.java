package com.connectedgames.identity.domain.device;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;


/**
 *Code that venue mngr gives to a display to pair with a table, 6 chars, valid for 10 mins
 * usable only once. Stored clear on purpouse, with only 36^6 vals hash will be reversed in seconds.
 * */
public record PairingCode(String value,UUID venueId,UUID tableId,Surface surface,Instant expiresAt,Instant usedAt) {
    public static final Duration VALIDITY = Duration.ofMinutes(10); //short lived , not ahshed
    private static final Pattern FORMAT = Pattern.compile("[A-Z0-9]{6}");
    public PairingCode {
        Objects.requireNonNull(value,"value");
        Objects.requireNonNull(venueId,"venueId");
        Objects.requireNonNull(tableId,"tableId");
        Objects.requireNonNull(surface,"surface");
        Objects.requireNonNull(expiresAt,"expiresAt");
        if(!FORMAT.matcher(value).matches()){
            throw new IllegalArgumentException("A pairing code is 6 characters, A-Z and 0-9: " + value);
        }
    }
    public static PairingCode issue(String value,UUID venueId,UUID tableId,Surface surface,Instant now)
    {
        return new PairingCode(value, venueId, tableId, surface, now.plus(VALIDITY), null);
    }
    public boolean isUsable(Instant now)
    {
        return usedAt==null&& now.isBefore(expiresAt);
    }
    public PairingCode used(Instant now)
    {
        if(!isUsable(now))
        {
            throw new IllegalStateException("Pairing code " + value + " is not usable");
        }
        return new PairingCode(value, venueId, tableId, surface, expiresAt, now);
    }
}


























