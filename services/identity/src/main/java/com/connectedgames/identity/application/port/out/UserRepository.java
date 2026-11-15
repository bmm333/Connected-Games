package com.connectedgames.identity.application.port.out;

import com.connectedgames.identity.domain.user.Email;
import com.connectedgames.identity.domain.user.User;
import com.connectedgames.identity.domain.user.UserId;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmail(Email email);
    Optional<User> findById(UserId id);
    void save(User user);
}
