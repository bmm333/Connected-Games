package com.connectedgames.catalog.adapters.out.persistence;

import com.connectedgames.catalog.TestcontainersConfiguration;
import com.connectedgames.catalog.domain.gametype.GameMode;
import com.connectedgames.catalog.domain.gametype.GameType;
import com.connectedgames.catalog.domain.gametype.GameTypeId;
import com.connectedgames.catalog.domain.table.QrCode;
import com.connectedgames.catalog.domain.table.Table;
import com.connectedgames.catalog.domain.table.TableStatus;
import com.connectedgames.catalog.domain.venue.Venue;
import com.connectedgames.catalog.domain.venue.VenueId;
import com.connectedgames.catalog.domain.venue.VenueType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** Runs the adapters against a real PostgreSQL migrated by Flyway). */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, VenuePersistenceAdapter.class,
        GameTypePersistenceAdapter.class, TablePersistenceAdapter.class})
class PersistenceAdaptersTest {

    private static final UUID ORGANIZATION = UUID.randomUUID();
    private static final UUID BELVEDERE = UUID.randomUUID();
    private static final UUID AURORA = UUID.randomUUID();
    private static final UUID FOOSBALL = UUID.randomUUID();

    @Autowired VenuePersistenceAdapter venues;
    @Autowired GameTypePersistenceAdapter gameTypes;
    @Autowired TablePersistenceAdapter tables;
    @Autowired DataSource dataSource;

    @BeforeEach
    void insertRows() {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.update("INSERT INTO catalog.organization (id, name) VALUES (?, 'Gruppo Belvedere')", ORGANIZATION);
        jdbc.update("INSERT INTO catalog.venue VALUES (?, ?, 'Bar Belvedere', 'Via del Belvedere 12', 'Roma', 'PUBLIC')", BELVEDERE, ORGANIZATION);
        jdbc.update("INSERT INTO catalog.venue VALUES (?, ?, 'Circolo Aurora', 'Corso Aurora 5', 'Torino', 'PUBLIC')", AURORA, ORGANIZATION);
        jdbc.update("INSERT INTO catalog.game_type VALUES (?, 'Calciobalilla', 'TEAM', 2, 10)", FOOSBALL);
        jdbc.update("INSERT INTO catalog.game_table VALUES (?, ?, ?, 'Calciobalilla verde - Tavolo 2', 'belvedere-t2', 'OCCUPIED')", UUID.randomUUID(), BELVEDERE, FOOSBALL);
        jdbc.update("INSERT INTO catalog.game_table VALUES (?, ?, ?, 'Calciobalilla verde - Tavolo 1', 'belvedere-t1', 'FREE')", UUID.randomUUID(), BELVEDERE, FOOSBALL);
        jdbc.update("INSERT INTO catalog.game_table VALUES (?, ?, ?, 'Calciobalilla - Tavolo 1', 'aurora-t1', 'FREE')", UUID.randomUUID(), AURORA, FOOSBALL);
    }

    @Test
    void findsATableByItsQrCodeAndMapsEveryColumn() {
        Table table = tables.findByCode(new QrCode("belvedere-t2")).orElseThrow();

        assertThat(table.name()).isEqualTo("Calciobalilla verde - Tavolo 2");
        assertThat(table.venueId()).isEqualTo(new VenueId(BELVEDERE));
        assertThat(table.gameTypeId()).isEqualTo(new GameTypeId(FOOSBALL));
        assertThat(table.status()).isEqualTo(TableStatus.OCCUPIED);
    }

    @Test
    void unknownQrCodeIsEmpty() {
        assertThat(tables.findByCode(new QrCode("unknowncode"))).isEmpty();
    }

    @Test
    void listsOnlyTheTablesOfOneVenueSortedByName() {
        assertThat(tables.findByVenue(new VenueId(BELVEDERE)))
                .extracting(Table::name)
                .containsExactly("Calciobalilla verde - Tavolo 1", "Calciobalilla verde - Tavolo 2");
    }

    @Test
    void filtersVenuesByCityIgnoringCase() {
        assertThat(venues.findAll(Optional.of("roma"))).extracting(Venue::name).containsExactly("Bar Belvedere");
        assertThat(venues.findAll(Optional.empty())).extracting(Venue::name).containsExactly("Bar Belvedere", "Circolo Aurora");
    }

    @Test
    void mapsAVenue() {
        Venue venue = venues.findById(new VenueId(BELVEDERE)).orElseThrow();

        assertThat(venue.city()).isEqualTo("Roma");
        assertThat(venue.type()).isEqualTo(VenueType.PUBLIC);
    }

    @Test
    void mapsAGameType() {
        GameType gameType = gameTypes.findById(new GameTypeId(FOOSBALL)).orElseThrow();

        assertThat(gameType.mode()).isEqualTo(GameMode.TEAM);
        assertThat(gameType.participantsPerSide()).isEqualTo(2);
        assertThat(gameType.pointsToWin()).isEqualTo(10);
        assertThat(gameTypes.findAll()).extracting(GameType::name).containsExactly("Calciobalilla");
    }
}
