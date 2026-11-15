package com.connectedgames.identity.application;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/** A clock the test moves forward by hand */
class TestClock extends Clock {
    private Instant now;

    TestClock(Instant now) { this.now = now; }

    void advance(Duration duration) { now = now.plus(duration); }

    @Override
    public Instant instant() { return now; }

    @Override
    public ZoneId getZone() { return ZoneOffset.UTC; }

    @Override
    public Clock withZone(ZoneId zone) { throw new UnsupportedOperationException(); }
}
