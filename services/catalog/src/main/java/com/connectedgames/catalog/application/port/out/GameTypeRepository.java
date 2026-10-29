package com.connectedgames.catalog.application.port.out;

import com.connectedgames.catalog.domain.gametype.GameType;
import com.connectedgames.catalog.domain.gametype.GameTypeId;

import java.util.List;
import java.util.Optional;

public interface GameTypeRepository{
    Optional<GameType> findById(GameTypeId id);
    List<GameType> findAll();
}
