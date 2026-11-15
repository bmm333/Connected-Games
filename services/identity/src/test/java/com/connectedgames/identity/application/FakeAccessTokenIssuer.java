package com.connectedgames.identity.application;

import java.time.Instant;
import java.util.List;

import com.connectedgames.identity.application.port.out.AccessToken;
import com.connectedgames.identity.application.port.out.AccessTokenIssuer;
import com.connectedgames.identity.domain.session.Session;
import com.connectedgames.identity.domain.user.RoleAssignment;

/** Remembers what it was asked to sign, so tests can check the claims without a real JWT */
class FakeAccessTokenIssuer implements AccessTokenIssuer {
    private Session lastSession;
    private List<RoleAssignment> lastRoles;

    @Override
    public AccessToken issue(Session session, List<RoleAssignment> roles, Instant now)
    {
        lastSession = session;
        lastRoles = roles;
        return new AccessToken("access-" + session.id().value(), now.plus(Session.ACCESS_TOKEN_LIFETIME));
    }

    Session lastSession() { return lastSession; }

    List<RoleAssignment> lastRoles() { return lastRoles; }
}
