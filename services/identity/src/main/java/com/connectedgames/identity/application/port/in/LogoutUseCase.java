package com.connectedgames.identity.application.port.in;

import com.connectedgames.identity.domain.session.SessionId;

public interface LogoutUseCase{

    void logout(SessionId sessionId);

}
