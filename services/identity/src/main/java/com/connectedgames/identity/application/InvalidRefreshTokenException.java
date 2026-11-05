package com.connectedgames.identity.application;

/** Refresh token unknown, already used, or of an ended session; the web adapter maps it to 401 */
public class InvalidRefreshTokenException extends RuntimeException {
    public InvalidRefreshTokenException()
    {
        super("Refresh token expired, revoked or already used");
    }
}
