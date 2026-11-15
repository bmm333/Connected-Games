package com.connectedgames.identity.application.port.in;

import com.connectedgames.identity.domain.user.Email;

public interface LoginUseCase {

    /** @throws com.connectedgames.identity.application.InvalidCredentialsException if the email or the password is wrong */
    UserSession login(Email email,String password);
}
