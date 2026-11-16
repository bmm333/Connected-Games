package com.connectedgames.match.infrastructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.connectedgames.match.application.GetCurrentMatchService;
import com.connectedgames.match.application.GetMatchService;
import com.connectedgames.match.application.GetMatchTimelineService;
import com.connectedgames.match.application.port.in.GetCurrentMatchUseCase;
import com.connectedgames.match.application.port.in.GetMatchTimelineUseCase;
import com.connectedgames.match.application.port.in.GetMatchUseCase;
import com.connectedgames.match.application.port.out.MatchEventRepository;
import com.connectedgames.match.application.port.out.MatchRepository;

@Configuration
class MatchConfiguration{

    @Bean
    GetCurrentMatchUseCase getCurrentMatch(MatchRepository matches){
        return new GetCurrentMatchService(matches);
    }
    @Bean
    GetMatchUseCase getMatch(MatchRepository matches)
    {
        return new GetMatchService(matches);
    }
    @Bean
    GetMatchTimelineUseCase getMatchTimeline(MatchRepository matches,MatchEventRepository events)
    {
        return new GetMatchTimelineService(matches,events);
    }



}
