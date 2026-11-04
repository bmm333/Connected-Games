package com.connectedgames.identity.application;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.connectedgames.identity.application.port.in.CreateGuestSessionUseCase;
import com.connectedgames.identity.application.port.in.IssuedSession;
import com.connectedgames.identity.application.port.out.AccessToken;
import com.connectedgames.identity.application.port.out.AccessTokenIssuer;
import com.connectedgames.identity.application.port.out.GuestRepository;
import com.connectedgames.identity.application.port.out.RefreshTokenGenerator;
import com.connectedgames.identity.application.port.out.RefreshTokenRepository;
import com.connectedgames.identity.application.port.out.SessionRepository;
import com.connectedgames.identity.domain.PlayerId;
import com.connectedgames.identity.domain.guest.Guest;
import com.connectedgames.identity.domain.guest.Nickname;
import com.connectedgames.identity.domain.session.RefreshToken;
import com.connectedgames.identity.domain.session.Session;
import com.connectedgames.identity.domain.session.SessionId;
import com.connectedgames.identity.domain.session.SessionKind;
import com.connectedgames.identity.domain.user.Role;
import com.connectedgames.identity.domain.user.RoleAssignment;

public final class CreateGuestSessionService implements CreateGuestSessionUseCase
{

    private final GuestRepository guests;
    private final SessionRepository sessions;
    private final RefreshTokenRepository refreshTokens;
    private final RefreshTokenGenerator tokenGenerator;
    private final AccessTokenIssuer accessTokens;
    private final Clock clock;

    public CreateGuestSessionService(GuestRepository guests,SessionRepository sessions,
            RefreshTokenRepository refreshTokens,RefreshTokenGenerator tokenGenerator,
            AccessTokenIssuer accessTokens,Clock clock)
    {
        this.guests=guests;
        this.sessions=sessions;
        this.refreshTokens=refreshTokens;
        this.tokenGenerator=tokenGenerator;
        this.accessTokens=accessTokens;
        this.clock=clock;
    }

    @Override
    public IssuedSession createGuestSession(Nickname nickname)
    {
        Instant now=clock.instant();

        Guest guest=new Guest(new PlayerId(UUID.randomUUID()),nickname);
        guests.save(guest);

        Session session=Session.start(new SessionId(UUID.randomUUID()),SessionKind.GUEST,
                guest.playerId().value(),now);
        sessions.save(session);

        // only the hash is stored; the plain token goes back to the client once
        String refreshToken=tokenGenerator.generate();
        refreshTokens.save(RefreshToken.issue(tokenGenerator.hash(refreshToken),session.id(),now));

        AccessToken accessToken=accessTokens.issue(session,List.of(RoleAssignment.of(Role.PLAYER)),now);

        return new IssuedSession(session.id(),session.kind(),accessToken.value(),accessToken.expiresAt(),
                refreshToken,session.refreshExpiresAt());
    }
}
