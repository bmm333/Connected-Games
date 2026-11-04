package com.connectedgames.identity.application.port.out;

import java.util.Optional;

import com.connectedgames.identity.domain.PlayerId;
import com.connectedgames.identity.domain.guest.Guest;

public interface GuestRepository{
    void save(Guest guest);
    Optional<Guest> findByPlayerId(PlayerId playerId);

}
