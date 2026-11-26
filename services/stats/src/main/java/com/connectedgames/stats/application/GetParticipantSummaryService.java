package com.connectedgames.stats.application;

import java.util.Optional;

import com.connectedgames.stats.application.port.in.GetParticipantSummaryUseCase;
import com.connectedgames.stats.application.port.out.StatsQueries;
import com.connectedgames.stats.domain.stats.ParticipantSummary;
import com.connectedgames.stats.domain.stats.StatsFilter;

public final class GetParticipantSummaryService implements GetParticipantSummaryUseCase {
    private final StatsQueries queries;

    public GetParticipantSummaryService(StatsQueries queries) {
        this.queries = queries;
    }

    @Override
    public Optional<ParticipantSummary> summary(Viewer viewer, String participantId, StatsFilter filter) {
        Access.mayReadParticipant(viewer, participantId);
        if (!queries.participantKnown(viewer, participantId)) {
            return Optional.empty();
        }
        return Optional.of(queries.participantSummary(viewer, participantId, filter));
    }
}
