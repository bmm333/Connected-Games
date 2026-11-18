package com.connectedgames.match.application.port.out;

import com.connectedgames.match.domain.EventId;

public record Rejection(EventId eventId,String reason) {

}
