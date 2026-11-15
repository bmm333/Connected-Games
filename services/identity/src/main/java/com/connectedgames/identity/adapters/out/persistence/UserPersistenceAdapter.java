package com.connectedgames.identity.adapters.out.persistence;

import com.connectedgames.identity.application.port.out.UserRepository;
import com.connectedgames.identity.domain.user.Email;
import com.connectedgames.identity.domain.user.User;
import com.connectedgames.identity.domain.user.UserId;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Implements the UserRepository port with JPA. */
@Component
class UserPersistenceAdapter implements UserRepository {

    private final UserJpaRepository jpa;

    UserPersistenceAdapter(UserJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return jpa.findByEmail(email.value()).map(UserEntity::toDomain);
    }

    @Override
    public Optional<User> findById(UserId id) {
        return jpa.findById(id.value()).map(UserEntity::toDomain);
    }

    @Override
    public void save(User user) {
        jpa.save(UserEntity.from(user));
    }
}
