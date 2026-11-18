package com.connectedgames.match.adapters.in.web;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.connectedgames.match.domain.TableId;

@RestController
class LiveController {

    private final TableStreams streams;
    LiveController(TableStreams streams){
        this.streams=streams;
    }
    @GetMapping(path="/tables/{tableId}/live",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    SseEmitter live(@PathVariable UUID tableId)
    {
        return streams.subscribe(new TableId(tableId));
    }
}
