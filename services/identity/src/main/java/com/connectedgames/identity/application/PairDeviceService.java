package com.connectedgames.identity.application;

import com.connectedgames.identity.application.port.in.DeviceSession;
import com.connectedgames.identity.application.port.in.PairDeviceUseCase;
import com.connectedgames.identity.application.port.out.DeviceRepository;
import com.connectedgames.identity.application.port.out.PairingCodeRepository;
import com.connectedgames.identity.application.port.out.SessionRepository;
import com.connectedgames.identity.domain.device.Device;
import com.connectedgames.identity.domain.device.DeviceId;
import com.connectedgames.identity.domain.device.PairingCode;
import com.connectedgames.identity.domain.device.Surface;
import com.connectedgames.identity.domain.session.Session;
import com.connectedgames.identity.domain.session.SessionId;
import com.connectedgames.identity.domain.session.SessionKind;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class PairDeviceService implements PairDeviceUseCase {

    private final PairingCodeRepository codes;
    private final DeviceRepository devices;
    private final SessionRepository sessions;
    private final SessionTokens tokens;
    private final Clock clock;

    public PairDeviceService(PairingCodeRepository codes, DeviceRepository devices, SessionRepository sessions,
                             SessionTokens tokens, Clock clock) {
        this.codes = codes;
        this.devices = devices;
        this.sessions = sessions;
        this.tokens = tokens;
        this.clock = clock;
    }

    @Override
    public DeviceSession pairDevice(String pairingCode, Surface surface) {
        Instant now = clock.instant();
        PairingCode code = codes.findByValueForUpdate(pairingCode)
                .filter(found -> found.isUsable(now) && found.surface() == surface)
                .orElseThrow(InvalidPairingCodeException::new);
        codes.save(code.used(now));

        devices.findByTable(code.tableId(), code.surface()).ifPresent(previous -> {
            sessions.findBySubject(previous.id().value()).forEach(session -> sessions.save(session.revoked(now)));
            devices.delete(previous.id());
        });

        Device device = new Device(new DeviceId(UUID.randomUUID()), code.venueId(), code.tableId(),
                code.surface(), now);
        devices.save(device);

        Session session = Session.start(new SessionId(UUID.randomUUID()), SessionKind.DEVICE,
                device.id().value(), now);
        sessions.save(session);

        return new DeviceSession(device, tokens.issue(session, List.of(), now));
    }
}
