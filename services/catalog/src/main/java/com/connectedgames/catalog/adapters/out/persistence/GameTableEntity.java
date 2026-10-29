package com.connectedgames.catalog.adapters.out.persistence;

import com.connectedgames.catalog.domain.gametype.GameTypeId;
import com.connectedgames.catalog.domain.table.QrCode;
import com.connectedgames.catalog.domain.table.Table;
import com.connectedgames.catalog.domain.table.TableId;
import com.connectedgames.catalog.domain.table.TableStatus;
import com.connectedgames.catalog.domain.venue.VenueId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

import java.util.UUID;

/**
 * Row of the "game_table" table. Venue and game type are plain ids, not JPA
 * relations: they are separate aggregates, loaded on their own.
 * The JPA annotation is written in full because "Table" is the domain class here.
 */
@Entity
@jakarta.persistence.Table(name = "game_table")
class GameTableEntity {

    @Id
    private UUID id;
    private UUID venueId;
    private UUID gameTypeId;
    private String name;
    private String qrCode;
    @Enumerated(EnumType.STRING)
    private TableStatus status;

    protected GameTableEntity() {
        // required by JPA
    }

    Table toDomain() {
        return new Table(new TableId(id), new VenueId(venueId), name, new QrCode(qrCode),
                new GameTypeId(gameTypeId), status);
    }
}
