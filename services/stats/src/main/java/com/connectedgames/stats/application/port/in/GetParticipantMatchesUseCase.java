package com.connectedgames.stats.application.port.in;

import java.util.List;
import java.util.Optional;

import com.connectedgames.stats.application.Viewer;
import com.connectedgames.stats.domain.stats.Page;
import com.connectedgames.stats.domain.stats.ParticipantMatch;
import com.connectedgames.stats.domain.stats.StatsFilter;

public interface GetParticipantMatchesUseCase {
    Optional<List<ParticipantMatch>> matches(Viewer viewer, String participantId, StatsFilter filter, Page page);
}
