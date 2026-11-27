package com.connectedgames.stats.infrastructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.connectedgames.stats.application.RecordMatchCompletedService;
import com.connectedgames.stats.application.port.in.RecordMatchCompletedUseCase;
import com.connectedgames.stats.application.port.out.MatchResultRepository;
import com.connectedgames.stats.application.GetLeaderboardService;
import com.connectedgames.stats.application.GetMatchResultService;
import com.connectedgames.stats.application.GetParticipantMatchesService;
import com.connectedgames.stats.application.GetParticipantSummaryService;
import com.connectedgames.stats.application.GetVenueSummaryService;
import com.connectedgames.stats.application.port.in.GetLeaderboardUseCase;
import com.connectedgames.stats.application.port.in.GetMatchResultUseCase;
import com.connectedgames.stats.application.port.in.GetParticipantMatchesUseCase;
import com.connectedgames.stats.application.port.in.GetParticipantSummaryUseCase;
import com.connectedgames.stats.application.port.in.GetVenueSummaryUseCase;
import com.connectedgames.stats.application.port.out.StatsQueries;


@Configuration
class StatsConfiguration {
    @Bean
    RecordMatchCompletedUseCase recordMatchCompleted(MatchResultRepository results)
    {
        return new RecordMatchCompletedService(results);
    }
        @Bean
    GetLeaderboardUseCase getLeaderboard(StatsQueries queries)
    {
        return new GetLeaderboardService(queries);
    }

    @Bean
    GetParticipantSummaryUseCase getParticipantSummary(StatsQueries queries)
    {
        return new GetParticipantSummaryService(queries);
    }

    @Bean
    GetParticipantMatchesUseCase getParticipantMatches(StatsQueries queries)
    {
        return new GetParticipantMatchesService(queries);
    }

    @Bean
    GetMatchResultUseCase getMatchResult(StatsQueries queries)
    {
        return new GetMatchResultService(queries);
    }

    @Bean
    GetVenueSummaryUseCase getVenueSummary(StatsQueries queries)
    {
        return new GetVenueSummaryService(queries);
    }
}
