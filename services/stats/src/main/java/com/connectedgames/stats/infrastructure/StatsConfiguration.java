package com.connectedgames.stats.infrastructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.connectedgames.stats.application.RecordMatchCompletedService;
import com.connectedgames.stats.application.port.in.RecordMatchCompletedUseCase;
import com.connectedgames.stats.application.port.out.MatchResultRepository;

@Configuration
class StatsConfiguration {
    @Bean
    RecordMatchCompletedUseCase recordMatchCompleted(MatchResultRepository results)
    {
        return new RecordMatchCompletedService(results);
    }
}
