package com.connectedgames.identity.application;

import com.connectedgames.identity.application.port.in.GetCurrentUserUseCase;
import com.connectedgames.identity.application.port.out.UserRepository;
import com.connectedgames.identity.domain.user.User;
import com.connectedgames.identity.domain.user.UserId;

public final class GetCurrentUserService implements GetCurrentUserUseCase
{
    private final UserRepository users;

    public GetCurrentUserService(UserRepository users)
    {
        this.users=users;
    }

    @Override
    public User getCurrentUser(UserId id)
    {
        // a valid USER token always names an existing account: a missing one is a bug, not a 404
        return users.findById(id)
                .orElseThrow(() -> new IllegalStateException("User " + id.value() + " does not exist"));
    }
}
