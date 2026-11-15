package com.connectedgames.identity.application.port.in;

/**
 * A session with its tokens and the profile of its owner. Sealed, so a switch over it
 * must handle every kind; DeviceSession joins with pairing (M2)
 * */
public sealed interface ActiveSession permits GuestSession, UserSession {
    IssuedSession session();
}
