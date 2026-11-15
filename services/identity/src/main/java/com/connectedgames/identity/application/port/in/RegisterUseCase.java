package com.connectedgames.identity.application.port.in;

import java.util.Optional;

import com.connectedgames.identity.domain.session.SessionId;
import com.connectedgames.identity.domain.user.Email;

public interface RegisterUseCase {

    /**
     * guestSession is the sid of the caller's access token when a guest registers: the new user keeps its PlayerId
     * @throws com.connectedgames.identity.application.EmailAlreadyRegisteredException if the email is taken
     */
    UserSession register(Email email,String password,String displayName,Optional<SessionId> guestSession);
}
