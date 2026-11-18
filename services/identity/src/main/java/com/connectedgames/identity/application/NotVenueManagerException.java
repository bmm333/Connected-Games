package com.connectedgames.identity.application;

/** The user is not the manager of the venue the request is about */
public class NotVenueManagerException extends RuntimeException {

    public NotVenueManagerException() {
        super("The user does not manage this venue");
    }
}
