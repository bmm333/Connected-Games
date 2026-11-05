package com.connectedgames.identity.application;

import java.time.Instant;
import java.util.List;

import com.connectedgames.identity.application.port.in.IssuedSession;
import com.connectedgames.identity.application.port.out.AccessToken;
import com.connectedgames.identity.application.port.out.AccessTokenIssuer;
import com.connectedgames.identity.application.port.out.RefreshTokenGenerator;
import com.connectedgames.identity.application.port.out.RefreshTokenRepository;
import com.connectedgames.identity.domain.session.RefreshToken;
import com.connectedgames.identity.domain.session.Session;
import com.connectedgames.identity.domain.user.Role;
import com.connectedgames.identity.domain.user.RoleAssignment;

/** Issues a new token pair for a session: shared by guest sessions, login and refresh */
public final class SessionTokens
{
    static final List<RoleAssignment> GUEST_ROLES=List.of(RoleAssignment.of(Role.PLAYER));

    private final RefreshTokenRepository refreshTokens;
    private final RefreshTokenGenerator tokenGenerator;
    private final AccessTokenIssuer accessTokens;

    public SessionTokens(RefreshTokenRepository refreshTokens,RefreshTokenGenerator tokenGenerator,
            AccessTokenIssuer accessTokens)
    {
        this.refreshTokens=refreshTokens;
        this.tokenGenerator=tokenGenerator;
        this.accessTokens=accessTokens;
    }

    IssuedSession issue(Session session,List<RoleAssignment> roles,Instant now)
    {
        // only the hash is stored; the plain token goes back to the client once
        String refreshToken=tokenGenerator.generate();
        refreshTokens.save(RefreshToken.issue(tokenGenerator.hash(refreshToken),session.id(),now));

        AccessToken accessToken=accessTokens.issue(session,roles,now);

        return new IssuedSession(session.id(),session.kind(),accessToken.value(),accessToken.expiresAt(),
                refreshToken,session.refreshExpiresAt());
    }
}
