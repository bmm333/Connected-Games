package com.connectedgames.stats.application;

import java.util.List;
import java.util.Optional;

import com.connectedgames.stats.application.port.in.GetParticipantMatchesUseCase;
import com.connectedgames.stats.application.port.out.StatsQueries;
import com.connectedgames.stats.domain.stats.Page;
import com.connectedgames.stats.domain.stats.ParticipantMatch;
import com.connectedgames.stats.domain.stats.StatsFilter;

public final class GetParticipantMatchesService implements GetParticipantMatchesUseCase {
    private final StatsQueries queries;

    public GetParticipantMatchesService(StatsQueries queries) {
        this.queries = queries;
    }

    @Override
    public Optional<List<ParticipantMatch>> matches(Viewer viewer, String participantId, StatsFilter filter,
                                                    Page page) {
        Access.mayReadParticipant(viewer, participantId);
        if (!queries.participantKnown(viewer, participantId)) {
            return Optional.empty();
        }
        return Optional.of(queries.participantMatches(viewer, participantId, filter, page));
    }
}
