package com.connectedgames.identity.application;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.connectedgames.identity.application.port.out.GuestRepository;
import com.connectedgames.identity.domain.PlayerId;
import com.connectedgames.identity.domain.guest.Guest;

class InMemoryGuestRepository implements GuestRepository {
    private final Map<PlayerId, Guest> guests = new HashMap<>();

    @Override
    public void save(Guest guest)
    {
        guests.put(guest.playerId(), guest);
    }

    @Override
    public Optional<Guest> findByPlayerId(PlayerId playerId)
    {
        return Optional.ofNullable(guests.get(playerId));
    }
}
