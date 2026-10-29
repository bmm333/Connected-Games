package com.connectedgames.catalog.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.connectedgames.catalog.application.port.out.GameTypeRepository;
import com.connectedgames.catalog.domain.gametype.GameType;
import com.connectedgames.catalog.domain.gametype.GameTypeId;

class InMemoryGameTypeRepository implements GameTypeRepository{
    private final List<GameType> gameTypes=new ArrayList<>();
    void add(GameType gameType) { gameTypes.add(gameType); }
    @Override
    public Optional<GameType> findById(GameTypeId id)
    {
        return gameTypes.stream().filter(g -> g.id().equals(id)).findFirst();
    }
    @Override
    public List<GameType> findAll()
    {
        return List.copyOf(gameTypes);
    }

    
}
