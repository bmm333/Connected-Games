package com.connectedgames.stats.application.port.in;

import java.util.Optional;

import com.connectedgames.stats.application.Viewer;
import com.connectedgames.stats.domain.stats.ParticipantSummary;
import com.connectedgames.stats.domain.stats.StatsFilter;

public interface GetParticipantSummaryUseCase {
    Optional<ParticipantSummary> summary(Viewer viewer, String participantId, StatsFilter filter);
}
