package com.connectedgames.identity.application.port.in;

import com.connectedgames.identity.domain.user.User;
import com.connectedgames.identity.domain.user.UserId;

public interface GetCurrentUserUseCase {

    /** id is the sub of a USER access token */
    User getCurrentUser(UserId id);
}
