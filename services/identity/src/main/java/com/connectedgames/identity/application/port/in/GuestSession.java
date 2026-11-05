package com.connectedgames.identity.application.port.in;

import com.connectedgames.identity.domain.guest.Guest;

/** A new guest with its session: the contract returns both, the profile and the tokens */
public record GuestSession(Guest guest,IssuedSession session) {}
