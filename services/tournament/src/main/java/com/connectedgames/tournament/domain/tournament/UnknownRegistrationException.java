package com.connectedgames.tournament.domain.tournament;

import com.connectedgames.tournament.domain.RegistrationId;

public class UnknownRegistrationException extends RuntimeException {

    public UnknownRegistrationException(RegistrationId id) {
        super("Registration " + id.value() + " not found");
    }
}
