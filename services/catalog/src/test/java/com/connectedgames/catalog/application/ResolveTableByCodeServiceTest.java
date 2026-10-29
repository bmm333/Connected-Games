package com.connectedgames.catalog.application;

import com.connectedgames.catalog.application.port.in.TableLookup;
import com.connectedgames.catalog.domain.gametype.GameType;
import com.connectedgames.catalog.domain.table.QrCode;
import com.connectedgames.catalog.domain.table.Table;
import com.connectedgames.catalog.domain.venue.Venue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.connectedgames.catalog.application.CatalogFixtures.foosball;
import static com.connectedgames.catalog.application.CatalogFixtures.table;
import static com.connectedgames.catalog.application.CatalogFixtures.venue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResolveTableByCodeServiceTest {

    private final InMemoryTableRepository tables = new InMemoryTableRepository();
    private final InMemoryVenueRepository venues = new InMemoryVenueRepository();
    private final InMemoryGameTypeRepository gameTypes = new InMemoryGameTypeRepository();
    private final ResolveTableByCodeService service = new ResolveTableByCodeService(tables, venues, gameTypes);

    private final Venue belvedere = venue("Bar Belvedere", "Roma");
    private final GameType foosball = foosball();
    private final Table table2 = table(belvedere, foosball, "Calciobalilla verde - Tavolo 2", "tavolo2belvedere");

    @BeforeEach
    void setUp() {
        venues.add(belvedere);
        gameTypes.add(foosball);
        tables.add(table2);
    }

    @Test
    void resolvesAKnownCodeToTableVenueAndGameType() {
        TableLookup lookup = service.resolve(new QrCode("tavolo2belvedere"));

        assertThat(lookup.table()).isEqualTo(table2);
        assertThat(lookup.venue()).isEqualTo(belvedere);
        assertThat(lookup.gameType()).isEqualTo(foosball);
    }

    @Test
    void unknownCodeIsNotFound() {
        assertThatThrownBy(() -> service.resolve(new QrCode("unknowncode")))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("unknowncode");
    }

    @Test
    void tableReferencingAMissingVenueIsADataError() {
        Venue deleted = venue("Locale cancellato", "Torino");
        tables.add(table(deleted, foosball, "Tavolo orfano", "orphantable1"));

        assertThatThrownBy(() -> service.resolve(new QrCode("orphantable1")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("missing venue");
    }

    @Test
    void tableReferencingAMissingGameTypeIsADataError() {
        tables.add(table(belvedere, foosball(), "Tavolo senza gioco", "nogametype1"));

        assertThatThrownBy(() -> service.resolve(new QrCode("nogametype1")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("missing game type");
    }
}
