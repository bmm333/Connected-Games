package com.connectedgames.identity.application;

import java.time.Clock;

import com.connectedgames.identity.application.port.in.LogoutUseCase;
import com.connectedgames.identity.application.port.out.SessionRepository;
import com.connectedgames.identity.domain.session.Session;
import com.connectedgames.identity.domain.session.SessionId;

public final class LogoutService implements LogoutUseCase{

    private final SessionRepository sessions;
    private final Clock clock;

    public LogoutService(SessionRepository sessions,Clock clock)
    {
        this.sessions=sessions;
        this.clock=clock;
    }
    
    @Override
    public  void logout(SessionId sessionId)
    {
        Session s= sessions.findById(sessionId).orElseThrow(() -> new IllegalStateException("Session " + sessionId.value() + " dose not exist"));
        sessions.save(s.revoked(clock.instant()));
    }   
    
}
