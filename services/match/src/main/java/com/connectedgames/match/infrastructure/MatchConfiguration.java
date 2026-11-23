package com.connectedgames.match.infrastructure;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import java.util.UUID;
import com.connectedgames.match.application.PublishMatchCompletedService;
import com.connectedgames.match.application.port.in.PublishMatchCompletedUseCase;
import com.connectedgames.match.application.port.out.MatchCompletedOutbox;
import com.connectedgames.match.application.port.out.MatchCompletedPublisher;
import com.connectedgames.match.application.AcknowledgeEventService;
import com.connectedgames.match.application.GetCurrentMatchService;
import com.connectedgames.match.application.GetMatchService;
import com.connectedgames.match.application.GetMatchTimelineService;
import com.connectedgames.match.application.RecordMatchEventService;
import com.connectedgames.match.application.port.in.AcknowledgeEventUseCase;
import com.connectedgames.match.application.port.in.GetCurrentMatchUseCase;
import com.connectedgames.match.application.port.in.GetMatchTimelineUseCase;
import com.connectedgames.match.application.port.in.GetMatchUseCase;
import com.connectedgames.match.application.port.in.RecordMatchEventUseCase;
import com.connectedgames.match.application.port.out.AckPublisher;
import com.connectedgames.match.application.port.out.MatchChangeNotifier;
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
    @Bean
    RecordMatchEventUseCase recordMatchEvent(MatchEventRepository events, MatchRepository matches,
                                         PlatformTransactionManager transactionManager,
                                         MatchChangeNotifier notifier,MatchCompletedOutbox outbox){
        return new TransactionalRecordMatchEvent(new RecordMatchEventService(events, matches, outbox, UUID::randomUUID),
            transactionManager, notifier);
    }
    @Bean
    PublishMatchCompletedUseCase publishMatchCompleted(MatchCompletedOutbox outbox, MatchCompletedPublisher publisher,
                                                   Clock clock) {
        return new PublishMatchCompletedService(outbox, publisher, clock);
    }
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    AcknowledgeEventUseCase acknowledgeEvent(AckPublisher publisher, Clock clock) {
        return new AcknowledgeEventService(publisher, clock);
    }
}
