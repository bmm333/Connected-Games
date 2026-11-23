package com.connectedgames.match.application;

import java.util.ArrayList;
import java.util.List;

import com.connectedgames.match.application.port.out.MatchCompleted;
import com.connectedgames.match.application.port.out.MatchCompletedPublisher;

class FakeMatchCompletedPublisher implements MatchCompletedPublisher {

    final List<MatchCompleted> published = new ArrayList<>();
    /** The broker takes this many messages and then refuses: -1 means it never refuses. */
    int refuseAfter = -1;

    @Override
    public void publish(MatchCompleted message) {
        if (refuseAfter >= 0 && published.size() >= refuseAfter) {
            throw new IllegalStateException("broker down");
        }
        published.add(message);
    }
}
