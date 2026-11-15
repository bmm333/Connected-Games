package com.connectedgames.identity.adapters.out.persistence;

import com.connectedgames.identity.domain.PlayerId;
import com.connectedgames.identity.domain.user.Email;
import com.connectedgames.identity.domain.user.PasswordHash;
import com.connectedgames.identity.domain.user.User;
import com.connectedgames.identity.domain.user.UserId;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Row of "user_account" with its roles. The roles are not an entity of their own:
 * they live and die with the user, and "position" keeps their order.
 */
@Entity
@Table(name = "user_account")
class UserEntity {

    @Id
    private UUID id;
    private String email;
    private String displayName;
    private String passwordHash;
    private UUID playerId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"))
    @OrderColumn(name = "position")
    private List<RoleAssignmentEmbeddable> roles = new ArrayList<>();

    protected UserEntity() {
        // required by JPA
    }

    static UserEntity from(User user) {
        UserEntity row = new UserEntity();
        row.id = user.id().value();
        row.email = user.email().value();
        row.displayName = user.displayName();
        row.passwordHash = user.passwordHash().value();
        row.playerId = user.playerId().value();
        row.roles = new ArrayList<>(user.roles().stream().map(RoleAssignmentEmbeddable::from).toList());
        return row;
    }

    User toDomain() {
        return new User(new UserId(id), new Email(email), displayName, new PasswordHash(passwordHash),
                new PlayerId(playerId), roles.stream().map(RoleAssignmentEmbeddable::toDomain).toList());
    }
}
