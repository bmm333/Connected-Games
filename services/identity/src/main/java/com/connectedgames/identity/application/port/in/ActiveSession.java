package com.connectedgames.identity.application.port.in;

/**
 * A session with its tokens and the profile of its owner. Sealed, so a switch over it
 * must handle every kind
 * */
public sealed interface ActiveSession permits GuestSession, UserSession, DeviceSession {
    IssuedSession session();
}
