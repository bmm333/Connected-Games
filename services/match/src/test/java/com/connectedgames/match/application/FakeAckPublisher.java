package com.connectedgames.match.application;

import java.util.ArrayList;
import java.util.List;

import com.connectedgames.match.application.port.out.Ack;
import com.connectedgames.match.application.port.out.AckPublisher;

class FakeAckPublisher implements AckPublisher {

    final List<Ack> published = new ArrayList<>();

    @Override
    public void publish(Ack ack) {
        published.add(ack);
    }
}
