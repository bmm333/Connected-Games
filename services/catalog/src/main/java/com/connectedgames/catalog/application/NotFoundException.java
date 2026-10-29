package com.connectedgames.catalog.application;

/**
 * A requested resource does not exist. The web adapter maps it to 404 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message)
    {
        super(message);
    }
}
