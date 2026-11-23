package com.connectedgames.match.application.port.out;


public interface MatchCompletedPublisher {

    /** Returns when the broker has the message. If it does not, throws: the message must stay in the outbox. */
    void publish(MatchCompleted message);
}
