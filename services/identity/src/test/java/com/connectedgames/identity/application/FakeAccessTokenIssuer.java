package com.connectedgames.identity.application;

import java.time.Instant;
import java.util.List;

import com.connectedgames.identity.application.port.out.AccessToken;
import com.connectedgames.identity.application.port.out.AccessTokenIssuer;
import com.connectedgames.identity.domain.PlayerId;
import com.connectedgames.identity.domain.device.Device;
import com.connectedgames.identity.domain.session.Session;
import com.connectedgames.identity.domain.user.RoleAssignment;

/** Remembers what it was asked to sign, so tests can check the claims without a real JWT */
class FakeAccessTokenIssuer implements AccessTokenIssuer {
    private Session lastSession;
    private PlayerId lastPlayerId;
    private List<RoleAssignment> lastRoles;
    private Device lastDevice;

    @Override
    public AccessToken issue(Session session, PlayerId playerId, List<RoleAssignment> roles, Instant now)
    {
        lastSession = session;
        lastPlayerId = playerId;
        lastRoles = roles;
        lastDevice = null;
        return new AccessToken("access-" + session.id().value(), now.plus(Session.ACCESS_TOKEN_LIFETIME));
    }

    @Override
    public AccessToken issueForDevice(Session session, Device device, Instant now)
    {
        lastSession = session;
        lastPlayerId = null;
        lastRoles = List.of();
        lastDevice = device;
        return new AccessToken("access-" + session.id().value(), now.plus(Session.ACCESS_TOKEN_LIFETIME));
    }

    Session lastSession() { return lastSession; }

    PlayerId lastPlayerId() { return lastPlayerId; }

    Device lastDevice() { return lastDevice; }

    List<RoleAssignment> lastRoles() { return lastRoles; }
}
