package com.connectedgames.tournament.domain.tournament;

import java.util.Objects;

import com.connectedgames.tournament.domain.GameTypeId;
import com.connectedgames.tournament.domain.standing.ScoringPolicy;

public record TournamentSetup(GameTypeId gameTypeId, int squadSize, FormatSpec format, ScoringPolicy scoring) {

    public TournamentSetup {
        Objects.requireNonNull(gameTypeId, "gameTypeId");
        Objects.requireNonNull(format, "format");
        Objects.requireNonNull(scoring, "scoring");
        if (squadSize < 1) {
            throw new IllegalArgumentException("squadSize starts from 1");
        }
    }

    public ParticipantKind participantKind() {
        return squadSize == 1 ? ParticipantKind.PLAYER : ParticipantKind.SQUAD;
    }
}
