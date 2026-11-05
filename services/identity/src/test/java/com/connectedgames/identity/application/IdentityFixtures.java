package com.connectedgames.identity.application;

import java.util.List;
import java.util.UUID;

import com.connectedgames.identity.domain.PlayerId;
import com.connectedgames.identity.domain.user.Email;
import com.connectedgames.identity.domain.user.RoleAssignment;
import com.connectedgames.identity.domain.user.User;
import com.connectedgames.identity.domain.user.UserId;

final class IdentityFixtures {

    private IdentityFixtures() {}

    static User user(String email, String password, List<RoleAssignment> roles) {
        return new User(new UserId(UUID.randomUUID()), new Email(email), "Mario Rossi",
                new FakePasswordHasher().hash(password), new PlayerId(UUID.randomUUID()), roles);
    }
}
