package com.connectedgames.match.application.port.out;

public interface AckPublisher {
    void publish(Ack ack);
}
