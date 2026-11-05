package com.connectedgames.identity.application;

/** Someone already registered with this email; the web adapter maps it to 409 */
public class EmailAlreadyRegisteredException extends RuntimeException {
    public EmailAlreadyRegisteredException()
    {
        super("Email already registered");
    }
}
