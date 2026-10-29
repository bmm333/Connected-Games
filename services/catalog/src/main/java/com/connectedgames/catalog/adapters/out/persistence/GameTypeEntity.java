package com.connectedgames.catalog.adapters.out.persistence;

import com.connectedgames.catalog.domain.gametype.GameMode;
import com.connectedgames.catalog.domain.gametype.GameType;
import com.connectedgames.catalog.domain.gametype.GameTypeId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/** Row of the "game_type" table. */
@Entity
@Table(name = "game_type")
class GameTypeEntity {

    @Id
    private UUID id;
    private String name;
    @Enumerated(EnumType.STRING)
    private GameMode mode;
    private int participantsPerSide;
    private int pointsToWin;

    protected GameTypeEntity() {
        // required by JPA
    }

    GameType toDomain() {
        return new GameType(new GameTypeId(id), name, mode, participantsPerSide, pointsToWin);
    }
}
