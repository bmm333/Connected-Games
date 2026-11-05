package com.connectedgames.identity.application.port.out;

import java.time.Instant;
import java.util.List;

import com.connectedgames.identity.domain.session.Session;
import com.connectedgames.identity.domain.user.RoleAssignment;

/** Signs the short-lived JWT the other services trust, with the sub, kind and roles of the session */
public interface AccessTokenIssuer {

    AccessToken issue(Session session,List<RoleAssignment> roles,Instant now);
}
