package com.connectedgames.catalog.adapters.in.web;

import com.connectedgames.catalog.application.NotFoundException;
import com.connectedgames.catalog.application.port.in.BrowseCatalogUseCase;
import com.connectedgames.catalog.application.port.in.ResolveTableByCodeUseCase;
import com.connectedgames.catalog.application.port.in.TableLookup;
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
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Use cases with fixed answers, web only http anwsers check. */
@TestConfiguration
class StubUseCases {

    static final Venue BELVEDERE = new Venue(new VenueId(UUID.randomUUID()), new OrganizationId(UUID.randomUUID()),
            "Bar Belvedere", "Via del Belvedere 12", "Roma", VenueType.PUBLIC);
    static final GameType FOOSBALL = new GameType(new GameTypeId(UUID.randomUUID()), "Calciobalilla", GameMode.TEAM, 2,
            10);
    static final Table TABLE = new Table(new TableId(UUID.randomUUID()), BELVEDERE.id(),
            "Calciobalilla verde - Tavolo 1",
            new QrCode("belvedere-t1"), FOOSBALL.id(), TableStatus.FREE);

    /** the real service throws IllegalStateException. */
    static final String BROKEN_CODE = "broken-t1";

    @Bean
    ResolveTableByCodeUseCase resolveTableByCode() {
        return code -> switch (code.value()) {
            case "belvedere-t1" -> new TableLookup(TABLE, BELVEDERE, FOOSBALL);
            case BROKEN_CODE -> throw new IllegalStateException("Table references a missing venue");
            default -> throw new NotFoundException("No table with code " + code.value());
        };
    }

    @Bean
    BrowseCatalogUseCase browseCatalog() {
        return new BrowseCatalogUseCase() {
            @Override
            public List<Venue> listVenues(Optional<String> city) {
                boolean matches = city.isEmpty() || city.get().equalsIgnoreCase(BELVEDERE.city());
                return matches ? List.of(BELVEDERE) : List.of();
            }

            @Override
            public Venue getVenue(VenueId id) {
                if (!id.equals(BELVEDERE.id())) {
                    throw new NotFoundException("No venue with id " + id.value());
                }
                return BELVEDERE;
            }

            @Override
            public List<Table> listTables(VenueId venueId) {
                getVenue(venueId);
                return List.of(TABLE);
            }

            @Override
            public List<GameType> listGameTypes() {
                return List.of(FOOSBALL);
            }
        };
    }
}
