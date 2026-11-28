package com.connectedgames.identity.adapters.out.persistence;

import com.connectedgames.identity.TestcontainersConfiguration;
import com.connectedgames.identity.domain.device.Device;
import com.connectedgames.identity.domain.device.DeviceId;
import com.connectedgames.identity.domain.device.PairingCode;
import com.connectedgames.identity.domain.device.Surface;
import com.connectedgames.identity.domain.session.Session;
import com.connectedgames.identity.domain.session.SessionId;
import com.connectedgames.identity.domain.session.SessionKind;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Devices, pairing codes and the sessions of a subject, on a real PostgreSQL migrated by Flyway. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, DevicePersistenceAdapter.class, PairingCodePersistenceAdapter.class,
        SessionPersistenceAdapter.class})
class DevicePersistenceAdaptersTest {

    private static final Instant NOW = Instant.parse("2026-11-18T20:00:00Z");

    @Autowired DevicePersistenceAdapter devices;
    @Autowired PairingCodePersistenceAdapter codes;
    @Autowired SessionPersistenceAdapter sessions;
    @Autowired TestEntityManager em;

    /** Writes to the database and empties the JPA cache, so the next read really hits the tables */
    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    private Device deviceAt(UUID tableId) {
        return new Device(new DeviceId(UUID.randomUUID()), UUID.randomUUID(), UUID.randomUUID(), tableId, Surface.KIOSK, NOW);
    }

    @Test
    void savesAndFindsADeviceByIdAndByTable() {
        Device device = deviceAt(UUID.randomUUID());

        devices.save(device);
        flushAndClear();

        assertThat(devices.findById(device.id())).contains(device);
        assertThat(devices.findByTable(device.tableId(), Surface.KIOSK)).contains(device);
        assertThat(devices.findByTable(UUID.randomUUID(), Surface.KIOSK)).isEmpty();
    }

    @Test
    void aTableHasOneDisplayPerSurface() {
        UUID table = UUID.randomUUID();
        devices.save(deviceAt(table));
        flushAndClear();

        devices.save(deviceAt(table));

        assertThatThrownBy(this::flushAndClear).isInstanceOf(PersistenceException.class);
    }

    @Test
    void theTableIsFreeAsSoonAsTheOldDisplayIsDeleted() {
        UUID table = UUID.randomUUID();
        Device old = deviceAt(table);
        devices.save(old);
        flushAndClear();

        // delete and insert in the same transaction, as pairing does: the delete must reach the database first
        devices.delete(old.id());
        Device replacement = deviceAt(table);
        devices.save(replacement);
        flushAndClear();

        assertThat(devices.findById(old.id())).isEmpty();
        assertThat(devices.findByTable(table, Surface.KIOSK)).contains(replacement);
    }

    @Test
    void savesAPairingCodeAndItsUse() {
        PairingCode code = PairingCode.issue("K7F2QX", UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), Surface.KIOSK, NOW);
        codes.save(code);
        flushAndClear();
        assertThat(codes.findByValueForUpdate("K7F2QX")).contains(code);

        PairingCode used = code.used(NOW.plusSeconds(5));
        codes.save(used);
        flushAndClear();

        assertThat(codes.findByValueForUpdate("K7F2QX")).contains(used);
        assertThat(codes.findByValueForUpdate("K7F2QX").orElseThrow().usedAt()).isEqualTo(NOW.plusSeconds(5));
    }

    @Test
    void anUnknownCodeIsNotFound() {
        assertThat(codes.findByValueForUpdate("NOPE00")).isEmpty();
    }

    @Test
    void theSameCodeIsReplacedWhenItIsGivenAgain() {
        UUID venue = UUID.randomUUID();
        codes.save(PairingCode.issue("AAAAAA", venue, UUID.randomUUID(), UUID.randomUUID(), Surface.KIOSK, NOW).used(NOW));
        flushAndClear();

        PairingCode again = PairingCode.issue("AAAAAA", venue, UUID.randomUUID(), UUID.randomUUID(), Surface.KIOSK, NOW.plusSeconds(3600));
        codes.save(again);
        flushAndClear();

        assertThat(codes.findByValueForUpdate("AAAAAA")).contains(again);
    }

    @Test
    void theDatabaseItselfRefusesACodeThatIsNotSixCharacters() {
        assertThatThrownBy(() -> {
            em.getEntityManager().createNativeQuery("""
                    insert into pairing_code (code, venue_id, table_id, surface, expires_at)
                    values ('abc', gen_random_uuid(), gen_random_uuid(), 'KIOSK', now())""").executeUpdate();
        }).isInstanceOf(PersistenceException.class);
    }

    @Test
    void findsTheSessionsOfASubjectAndOnlyThose() {
        UUID subject = UUID.randomUUID();
        Session first = Session.start(new SessionId(UUID.randomUUID()), SessionKind.DEVICE, subject, NOW);
        Session second = Session.start(new SessionId(UUID.randomUUID()), SessionKind.DEVICE, subject, NOW.plusSeconds(60));
        Session other = Session.start(new SessionId(UUID.randomUUID()), SessionKind.DEVICE, UUID.randomUUID(), NOW);
        sessions.save(first);
        sessions.save(second);
        sessions.save(other);
        flushAndClear();

        assertThat(sessions.findBySubject(subject)).containsExactlyInAnyOrder(first, second);
        assertThat(sessions.findBySubject(UUID.randomUUID())).isEmpty();
    }
}
