package com.connectedgames.identity.adapters.in.web;

import com.connectedgames.identity.adapters.in.web.WebRequests.RegisterRequest;
import com.connectedgames.identity.adapters.in.web.WebResponses.SessionResponse;
import com.connectedgames.identity.adapters.in.web.WebResponses.UserResponse;
import com.connectedgames.identity.application.port.in.GetCurrentUserUseCase;
import com.connectedgames.identity.application.port.in.RegisterUseCase;
import com.connectedgames.identity.domain.session.SessionId;
import com.connectedgames.identity.domain.session.SessionKind;
import com.connectedgames.identity.domain.user.Email;
import com.connectedgames.identity.domain.user.UserId;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

/** Users endpoints of contracts/openapi/identity.yaml: registration and the current profile */
@RestController
class UserController {

    private final RegisterUseCase register;
    private final GetCurrentUserUseCase getCurrentUser;

    UserController(RegisterUseCase register, GetCurrentUserUseCase getCurrentUser) {
        this.register = register;
        this.getCurrentUser = getCurrentUser;
    }

    /** The token is optional: only a GUEST token makes the new user take over the guest's player */
    @PostMapping("/users")
    ResponseEntity<SessionResponse> register(@Valid @RequestBody RegisterRequest request,
                                             @AuthenticationPrincipal Jwt token) {
        Optional<SessionId> guestSession = Optional.ofNullable(token)
                .filter(t -> AccessTokens.kind(t) == SessionKind.GUEST)
                .map(AccessTokens::sessionId);
        var registered = register.register(new Email(request.email()), request.password(),
                request.displayName(), guestSession);
        return ResponseEntity.status(HttpStatus.CREATED).cacheControl(CacheControl.noStore())
                .body(SessionResponse.from(registered));
    }

    @GetMapping("/users/me")
    UserResponse getCurrentUser(@AuthenticationPrincipal Jwt token) {
        // for a guest or a device the sub is not an account
        if (AccessTokens.kind(token) != SessionKind.USER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only a user session has a profile");
        }
        return UserResponse.from(getCurrentUser.getCurrentUser(new UserId(AccessTokens.subject(token))));
    }
}
