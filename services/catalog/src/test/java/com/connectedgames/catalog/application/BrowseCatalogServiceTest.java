package com.connectedgames.catalog.application;

import com.connectedgames.catalog.domain.gametype.GameType;
import com.connectedgames.catalog.domain.table.Table;
import com.connectedgames.catalog.domain.venue.Venue;
import com.connectedgames.catalog.domain.venue.VenueId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static com.connectedgames.catalog.application.CatalogFixtures.foosball;
import static com.connectedgames.catalog.application.CatalogFixtures.table;
import static com.connectedgames.catalog.application.CatalogFixtures.venue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrowseCatalogServiceTest {

    private final InMemoryVenueRepository venues = new InMemoryVenueRepository();
    private final InMemoryTableRepository tables = new InMemoryTableRepository();
    private final InMemoryGameTypeRepository gameTypes = new InMemoryGameTypeRepository();
    private final BrowseCatalogService service = new BrowseCatalogService(venues, tables, gameTypes);

    private final Venue belvedere = venue("Bar Belvedere", "Roma");
    private final Venue aurora = venue("Circolo Aurora", "Torino");
    private final GameType foosball = foosball();
    private final Table belvedereTable = table(belvedere, foosball, "Calciobalilla verde - Tavolo 2", "tavolo2belvedere");
    private final Table auroraTable = table(aurora, foosball, "Calciobalilla - Tavolo 1", "tavolo1aurora");

    @BeforeEach
    void setUp() {
        venues.add(belvedere);
        venues.add(aurora);
        gameTypes.add(foosball);
        tables.add(belvedereTable);
        tables.add(auroraTable);
    }

    @Test
    void listsAllVenuesWithoutACityFilter() {
        assertThat(service.listVenues(Optional.empty())).containsExactlyInAnyOrder(belvedere, aurora);
    }

    @Test
    void filtersVenuesByCityIgnoringCase() {
        assertThat(service.listVenues(Optional.of("roma"))).containsExactly(belvedere);
    }

    @Test
    void getsAnExistingVenue() {
        assertThat(service.getVenue(belvedere.id())).isEqualTo(belvedere);
    }

    @Test
    void unknownVenueIsNotFound() {
        VenueId unknown = new VenueId(UUID.randomUUID());

        assertThatThrownBy(() -> service.getVenue(unknown)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void listsOnlyTheTablesOfTheRequestedVenue() {
        assertThat(service.listTables(belvedere.id())).containsExactly(belvedereTable);
    }

    @Test
    void tablesOfAnUnknownVenueAreNotFoundRatherThanAnEmptyList() {
        VenueId unknown = new VenueId(UUID.randomUUID());

        assertThatThrownBy(() -> service.listTables(unknown)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void listsGameTypes() {
        assertThat(service.listGameTypes()).containsExactly(foosball);
    }
}
