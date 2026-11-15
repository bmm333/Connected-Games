package com.connectedgames.identity.application;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.connectedgames.identity.application.port.out.UserRepository;
import com.connectedgames.identity.domain.user.Email;
import com.connectedgames.identity.domain.user.User;
import com.connectedgames.identity.domain.user.UserId;

class InMemoryUserRepository implements UserRepository {
    private final Map<UserId, User> users = new HashMap<>();

    @Override
    public Optional<User> findByEmail(Email email)
    {
        return users.values().stream().filter(u -> u.email().equals(email)).findFirst();
    }

    @Override
    public Optional<User> findById(UserId id)
    {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public void save(User user)
    {
        users.put(user.id(), user);
    }
}
