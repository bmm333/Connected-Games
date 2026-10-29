package com.connectedgames.catalog.application;

import com.connectedgames.catalog.domain.OrganizationId;
import com.connectedgames.catalog.domain.gametype.GameMode;
import com.connectedgames.catalog.domain.gametype.GameType;
import com.connectedgames.catalog.domain.gametype.GameTypeId;
import com.connectedgames.catalog.domain.table.QrCode;
import com.connectedgames.catalog.domain.table.Table;
import com.connectedgames.catalog.domain.table.TableId;
import com.connectedgames.catalog.domain.table.TableStatus;
import com.connectedgames.catalog.domain.venue.Venue;
import com.connectedgames.catalog.domain.venue.VenueId;
import com.connectedgames.catalog.domain.venue.VenueType;

import java.util.UUID;

/** Sample data shared by the application tests */
final class CatalogFixtures {

    private CatalogFixtures() {
    }

    static Venue venue(String name, String city) {
        return new Venue(new VenueId(UUID.randomUUID()), new OrganizationId(UUID.randomUUID()),
                name, "Via Roma 1", city, VenueType.PUBLIC);
    }

    static GameType foosball() {
        return new GameType(new GameTypeId(UUID.randomUUID()), "Calciobalilla", GameMode.TEAM, 2, 10);
    }

    static Table table(Venue venue, GameType gameType, String name, String code) {
        return new Table(new TableId(UUID.randomUUID()), venue.id(), name, new QrCode(code),
                gameType.id(), TableStatus.FREE);
    }
}
