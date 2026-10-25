package com.connectedgames.catalog.domain.table;

import com.connectedgames.catalog.domain.gametype.GameTypeId;
import com.connectedgames.catalog.domain.venue.VenueId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class TableTest {

    private Table tableWith(String name, TableStatus status) {
        return new Table(new TableId(UUID.randomUUID()), new VenueId(UUID.randomUUID()), name,
                new QrCode("tavolo2belvedere"), new GameTypeId(UUID.randomUUID()), status);
    }

    @Test
    void rejectsBlankName() {
        assertThatIllegalArgumentException().isThrownBy(() -> tableWith("  ", TableStatus.FREE));
    }

    @Test
    void isAvailableOnlyWhenFree() {
        assertThat(tableWith("Calciobalilla verde - Tavolo 2", TableStatus.FREE).isAvailable()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = TableStatus.class, names = "FREE", mode = EnumSource.Mode.EXCLUDE)
    void isNotAvailableInAnyOtherStatus(TableStatus status) {
        assertThat(tableWith("Calciobalilla verde - Tavolo 2", status).isAvailable()).isFalse();
    }
}
