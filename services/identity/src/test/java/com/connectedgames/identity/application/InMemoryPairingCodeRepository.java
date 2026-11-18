package com.connectedgames.identity.application;

import com.connectedgames.identity.application.port.out.PairingCodeRepository;
import com.connectedgames.identity.domain.device.PairingCode;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class InMemoryPairingCodeRepository implements PairingCodeRepository {

    private final Map<String, PairingCode> codes = new HashMap<>();

    @Override
    public Optional<PairingCode> findByValueForUpdate(String value) {
        return Optional.ofNullable(codes.get(value));
    }

    @Override
    public void save(PairingCode code) {
        codes.put(code.value(), code);
    }
}
