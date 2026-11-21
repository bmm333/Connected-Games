package com.connectedgames.match.application.port.in;

public interface PublishMatchCompletedUseCase {

    /** Sends the messages waiting in the outbox, in order. Returns how many the broker took. */
    int publishPending();
}

