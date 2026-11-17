package com.connectedgames.match.adapters.in.web;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.connectedgames.match.adapters.in.web.WebResponses.MatchResponse;
import com.connectedgames.match.application.MatchNotFoundException;
import com.connectedgames.match.application.port.in.GetCurrentMatchUseCase;
import com.connectedgames.match.application.port.in.GetMatchUseCase;
import com.connectedgames.match.application.port.out.MatchChangeNotifier;
import com.connectedgames.match.domain.MatchId;
import com.connectedgames.match.domain.TableId;
import com.connectedgames.match.domain.match.Match;

import jakarta.annotation.PreDestroy;

/**
 * Open streams of each table, + the way to reach them. Match is sent from here, from the commited data
 * Client never sees a state that could still be rolled back.
 * */
@Component
class TableStreams implements MatchChangeNotifier{
    private static final Logger log = LoggerFactory.getLogger(TableStreams.class);
    /**Browser reconnects by itself and gets curr match again*/
    private static final long TIMEOUT_MS = Duration.ofMinutes(30).toMillis();
    private final GetCurrentMatchUseCase getCurrentMatch;
    private final GetMatchUseCase getMatch;
    private final Map<TableId,List<SseEmitter>> emitters = new ConcurrentHashMap<>();
    //one thread for evth thats sent: updates leave in order of creation.
    //slow  client cnant stop thread that recives events from hub
    private final ExecutorService sender= Executors.newSingleThreadExecutor(
        Thread.ofPlatform().name("table-streams").daemon().factory());
    private final ScheduledExecutorService heartbeat = Executors.newSingleThreadScheduledExecutor(
        Thread.ofPlatform().name("table-streams-heartbeat").daemon().factory());
    TableStreams(GetCurrentMatchUseCase getCurrentMatch,GetMatchUseCase getMatch, @Value("${match.live.heartbeat:20s}") Duration heartbeatEvery){
        this.getCurrentMatch=getCurrentMatch;
        this.getMatch=getMatch;
        // table can send nothing for hours, but that means without sing of life , proxies close the conn. 
        heartbeat.scheduleWithFixedDelay(() -> execute(this::ping), heartbeatEvery.toMillis(), heartbeatEvery.toMillis(), TimeUnit.MILLISECONDS);
    }

    SseEmitter subscribe(TableId tableId)
    {
        SseEmitter emitter=new SseEmitter(TIMEOUT_MS);
        emitters.compute(tableId,(id,list)-> {
            List<SseEmitter> updated=list==null ? new CopyOnWriteArrayList<>() : list;
            updated.add(emitter);
            return updated;
        });
        emitter.onCompletion(() -> forget(tableId,emitter));
        emitter.onTimeout(emitter::complete);
        emitter.onError(error -> forget(tableId, emitter));
        execute(() -> currentMatch(tableId).ifPresent(match -> send(tableId, emitter, match)));
        return emitter;
    }
    @Override
    public void matchChanged(TableId tableId, MatchId matchId) {
        if (!emitters.containsKey(tableId)) {
            return;
        }
        execute(() -> {
            try {
                Match match = getMatch.getMatch(matchId);
                emitters.getOrDefault(tableId, List.of()).forEach(emitter -> send(tableId, emitter, match));
            } catch (RuntimeException e) {
                log.warn("Live update of table {} lost", tableId.value(), e);
            }
        });
    }

    private Optional<Match> currentMatch(TableId tableId) {
        try {
            return Optional.of(getCurrentMatch.getCurrentMatch(tableId));
        } catch (MatchNotFoundException e) {
            return Optional.empty();
        }
    }
    private void send(TableId tableId,SseEmitter emitter, Match match)
    {
        deliver(tableId,emitter,SseEmitter.event().data(MatchResponse.from(match),MediaType.APPLICATION_JSON));
    }
    private void ping()
    {
        emitters.forEach((tableId,list)->
            list.forEach(emitter -> deliver(tableId,emitter,SseEmitter.event().comment("keep-alive"))));    
    }
    private void deliver(TableId tableId, SseEmitter emitter, SseEmitter.SseEventBuilder event) {
        try {
            emitter.send(event);
        } catch (IOException | RuntimeException e) {
            // The client went away, forget it
            forget(tableId, emitter);
            emitter.completeWithError(e);
        }
    }

    private void forget(TableId tableId, SseEmitter emitter) {
        emitters.computeIfPresent(tableId, (id, list) -> {
            list.remove(emitter);
            return list.isEmpty() ? null : list;
        });
    }

    private void execute(Runnable task) {
        try {
            sender.execute(task);
        } catch (RejectedExecutionException e) {
            // shutting down
        }
    }

    @PreDestroy
    void close() {
        heartbeat.shutdownNow();
        sender.shutdownNow();
        emitters.values().forEach(list -> list.forEach(SseEmitter::complete));
    }
}





