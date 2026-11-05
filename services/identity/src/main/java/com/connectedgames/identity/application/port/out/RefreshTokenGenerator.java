package com.connectedgames.identity.application.port.out;

/**
 * Random refresh tokens. hash is a plain SHA-256, not bcrypt: the same token must always give
 * the same hash, so the token can be looked up by it
 * */
public interface RefreshTokenGenerator {

    String generate();
    String hash(String token);
}
