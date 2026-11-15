package com.connectedgames.identity.application;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import com.connectedgames.identity.application.port.in.CreateGuestSessionUseCase;
import com.connectedgames.identity.application.port.in.GuestSession;
import com.connectedgames.identity.application.port.out.GuestRepository;
import com.connectedgames.identity.application.port.out.SessionRepository;
import com.connectedgames.identity.domain.PlayerId;
import com.connectedgames.identity.domain.guest.Guest;
import com.connectedgames.identity.domain.guest.Nickname;
import com.connectedgames.identity.domain.session.Session;
import com.connectedgames.identity.domain.session.SessionId;
import com.connectedgames.identity.domain.session.SessionKind;

public final class CreateGuestSessionService implements CreateGuestSessionUseCase
{

    private final GuestRepository guests;
    private final SessionRepository sessions;
    private final SessionTokens tokens;
    private final Clock clock;

    public CreateGuestSessionService(GuestRepository guests,SessionRepository sessions,SessionTokens tokens,
            Clock clock)
    {
        this.guests=guests;
        this.sessions=sessions;
        this.tokens=tokens;
        this.clock=clock;
    }

    @Override
    public GuestSession createGuestSession(Nickname nickname)
    {
        Instant now=clock.instant();

        Guest guest=new Guest(new PlayerId(UUID.randomUUID()),nickname);
        guests.save(guest);

        Session session=Session.start(new SessionId(UUID.randomUUID()),SessionKind.GUEST,
                guest.playerId().value(),now);
        sessions.save(session);

        return new GuestSession(guest,tokens.issue(session,SessionTokens.GUEST_ROLES,now));
    }
}
