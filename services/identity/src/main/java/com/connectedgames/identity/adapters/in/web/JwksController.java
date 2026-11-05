package com.connectedgames.identity.adapters.in.web;

import com.nimbusds.jose.jwk.JWKSet;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Publishes the public key: the other services download it to verify our tokens */
@RestController
class JwksController {

    private final JWKSet publicKeys;

    JwksController(JWKSet publicKeys) {
        this.publicKeys = publicKeys;
    }

    @GetMapping("/.well-known/jwks.json")
    Map<String, Object> jwks() {
        return publicKeys.toJSONObject();
    }
}
