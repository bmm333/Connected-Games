package com.connectedgames.match.adapters.out.persistence;

import com.connectedgames.match.domain.Side;
import com.connectedgames.match.domain.match.MatchSide;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/** One row of "match_side", owned by its match. */
@Embeddable
class MatchSideEmbeddable {

    @Enumerated(EnumType.STRING)
    private Side side;
    private String participantId;
    private String displayName;
    private int score;

    protected MatchSideEmbeddable() {
    }

    static MatchSideEmbeddable from(MatchSide matchSide) {
        MatchSideEmbeddable row = new MatchSideEmbeddable();
        row.side = matchSide.side();
        row.participantId = matchSide.participantId();
        row.displayName = matchSide.displayName();
        row.score = matchSide.score();
        return row;
    }

    MatchSide toDomain() {
        return new MatchSide(side, participantId, displayName, score);
    }
}
