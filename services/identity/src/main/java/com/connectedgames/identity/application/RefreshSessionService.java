package com.connectedgames.identity.application;

import java.time.Clock;
import java.time.Instant;

import com.connectedgames.identity.application.port.in.ActiveSession;
import com.connectedgames.identity.application.port.in.GuestSession;
import com.connectedgames.identity.application.port.in.RefreshSessionUseCase;
import com.connectedgames.identity.application.port.in.UserSession;
import com.connectedgames.identity.application.port.out.GuestRepository;
import com.connectedgames.identity.application.port.out.RefreshTokenGenerator;
import com.connectedgames.identity.application.port.out.RefreshTokenRepository;
import com.connectedgames.identity.application.port.out.SessionRepository;
import com.connectedgames.identity.application.port.out.UserRepository;
import com.connectedgames.identity.domain.PlayerId;
import com.connectedgames.identity.domain.guest.Guest;
import com.connectedgames.identity.domain.session.RefreshToken;
import com.connectedgames.identity.domain.session.Session;
import com.connectedgames.identity.domain.user.User;
import com.connectedgames.identity.domain.user.UserId;

public final class RefreshSessionService implements RefreshSessionUseCase
{
    private final RefreshTokenRepository refreshTokens;
    private final RefreshTokenGenerator tokenGenerator;
    private final SessionRepository sessions;
    private final GuestRepository guests;
    private final UserRepository users;
    private final SessionTokens tokens;
    private final Clock clock;

    public RefreshSessionService(RefreshTokenRepository refreshTokens,RefreshTokenGenerator tokenGenerator,
            SessionRepository sessions,GuestRepository guests,UserRepository users,SessionTokens tokens,Clock clock)
    {
        this.refreshTokens=refreshTokens;
        this.tokenGenerator=tokenGenerator;
        this.sessions=sessions;
        this.guests=guests;
        this.users=users;
        this.tokens=tokens;
        this.clock=clock;
    }

    @Override
    public ActiveSession refreshSession(String refreshToken)
    {
        Instant now=clock.instant();

        RefreshToken token=refreshTokens.findByHash(tokenGenerator.hash(refreshToken))
                .orElseThrow(InvalidRefreshTokenException::new);
        Session session=sessions.findById(token.sessionId())
                .orElseThrow(() -> new IllegalStateException("Refresh token references a missing session"));

        // a used token sent again means someone has a copy: end the session for whoever holds it
        if(token.isUsed())
        {
            sessions.save(session.revoked(now));
            throw new InvalidRefreshTokenException();
        }
        if(!session.isActive(now))
        {
            throw new InvalidRefreshTokenException();
        }

        refreshTokens.save(token.used(now));
        Session refreshed=session.refreshed(now);
        sessions.save(refreshed);

        return switch(refreshed.kind())
        {
            case GUEST -> guestSession(refreshed,now);
            case USER -> userSession(refreshed,now);
            case DEVICE -> throw new UnsupportedOperationException("Device sessions come with pairing (M2)");
        };
    }

    private GuestSession guestSession(Session session,Instant now)
    {
        Guest guest=guests.findByPlayerId(new PlayerId(session.subjectId()))
                .orElseThrow(() -> new IllegalStateException("Session " + session.id().value() + " references a missing guest"));
        return new GuestSession(guest,tokens.issue(session,SessionTokens.GUEST_ROLES,now));
    }

    // roles are read again on every refresh, so a removed role is gone within one access token lifetime
    private UserSession userSession(Session session,Instant now)
    {
        User user=users.findById(new UserId(session.subjectId()))
                .orElseThrow(() -> new IllegalStateException("Session " + session.id().value() + " references a missing user"));
        return new UserSession(user,tokens.issue(session,user.roles(),now));
    }
}
