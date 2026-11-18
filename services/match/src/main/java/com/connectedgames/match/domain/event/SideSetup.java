package com.connectedgames.match.domain.event;

import java.util.Objects;

import com.connectedgames.match.domain.Side;

/** participantId and displayName are null when the match started offline without login. */
public record SideSetup(Side side, String participantId, String displayName) {

    public SideSetup {
        Objects.requireNonNull(side, "side");
    }
}
