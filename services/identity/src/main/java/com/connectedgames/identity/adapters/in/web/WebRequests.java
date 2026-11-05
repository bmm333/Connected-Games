package com.connectedgames.identity.adapters.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** JSON bodies received by the identity API, with the limits of contracts/openapi/identity.yaml */
final class WebRequests {

    private WebRequests() {
        // request container only
    }

    record GuestSessionRequest(@NotNull @Size(min = 2, max = 24) String nickname) {}

    record LoginRequest(@NotBlank @Size(max = 254) String email, @NotNull @Size(max = 64) String password) {}

    record RefreshRequest(@NotBlank String refreshToken) {}

    record RegisterRequest(@NotBlank @Size(max = 254) String email,
                           @NotNull @Size(min = 8, max = 64) String password,
                           @NotNull @Size(min = 2, max = 40) String displayName) {}
}
