package com.connectedgames.identity.adapters.in.web;

import com.connectedgames.identity.adapters.in.web.WebRequests.GuestSessionRequest;
import com.connectedgames.identity.adapters.in.web.WebRequests.LoginRequest;
import com.connectedgames.identity.adapters.in.web.WebRequests.RefreshRequest;
import com.connectedgames.identity.adapters.in.web.WebResponses.SessionResponse;
import com.connectedgames.identity.application.port.in.ActiveSession;
import com.connectedgames.identity.application.port.in.CreateGuestSessionUseCase;
import com.connectedgames.identity.application.port.in.LoginUseCase;
import com.connectedgames.identity.application.port.in.LogoutUseCase;
import com.connectedgames.identity.application.port.in.RefreshSessionUseCase;
import com.connectedgames.identity.domain.guest.Nickname;
import com.connectedgames.identity.domain.user.Email;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Sessions endpoints of contracts/openapi/identity.yaml: guest, login, refresh and logout */
@RestController
class SessionController {

    private final CreateGuestSessionUseCase createGuestSession;
    private final LoginUseCase login;
    private final RefreshSessionUseCase refreshSession;
    private final LogoutUseCase logout;

    SessionController(CreateGuestSessionUseCase createGuestSession, LoginUseCase login,
                      RefreshSessionUseCase refreshSession, LogoutUseCase logout) {
        this.createGuestSession = createGuestSession;
        this.login = login;
        this.refreshSession = refreshSession;
        this.logout = logout;
    }

    @PostMapping("/sessions/guest")
    ResponseEntity<SessionResponse> createGuestSession(@Valid @RequestBody GuestSessionRequest request) {
        return withTokens(createGuestSession.createGuestSession(new Nickname(request.nickname())));
    }

    @PostMapping("/sessions")
    ResponseEntity<SessionResponse> login(@Valid @RequestBody LoginRequest request) {
        return withTokens(login.login(new Email(request.email()), request.password()));
    }

    @PostMapping("/sessions/refresh")
    ResponseEntity<SessionResponse> refreshSession(@Valid @RequestBody RefreshRequest request) {
        return withTokens(refreshSession.refreshSession(request.refreshToken()));
    }

    @DeleteMapping("/sessions/current")
    ResponseEntity<Void> logout(@AuthenticationPrincipal Jwt token) {
        logout.logout(AccessTokens.sessionId(token));
        return ResponseEntity.noContent().build();
    }

    /** 200 with the session; tokens must never end up in a cache */
    static ResponseEntity<SessionResponse> withTokens(ActiveSession session) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(SessionResponse.from(session));
    }
}
