package com.connectedgames.identity.adapters.out.persistence;

import com.connectedgames.identity.domain.PlayerId;
import com.connectedgames.identity.domain.guest.Guest;
import com.connectedgames.identity.domain.guest.Nickname;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/** Row of the "guest" table. */
@Entity
@Table(name = "guest")
class GuestEntity {

    @Id
    private UUID playerId;
    private String nickname;

    protected GuestEntity() {
        // required by JPA
    }

    static GuestEntity from(Guest guest) {
        GuestEntity row = new GuestEntity();
        row.playerId = guest.playerId().value();
        row.nickname = guest.nickname().value();
        return row;
    }

    Guest toDomain() {
        return new Guest(new PlayerId(playerId), new Nickname(nickname));
    }
}
