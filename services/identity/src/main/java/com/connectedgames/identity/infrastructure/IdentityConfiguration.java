package com.connectedgames.identity.infrastructure;

import com.connectedgames.identity.application.CreateGuestSessionService;
import com.connectedgames.identity.application.GetCurrentUserService;
import com.connectedgames.identity.application.LoginService;
import com.connectedgames.identity.application.LogoutService;
import com.connectedgames.identity.application.RefreshSessionService;
import com.connectedgames.identity.application.RegisterService;
import com.connectedgames.identity.application.SessionTokens;
import com.connectedgames.identity.application.port.in.CreateGuestSessionUseCase;
import com.connectedgames.identity.application.port.in.GetCurrentUserUseCase;
import com.connectedgames.identity.application.port.in.LoginUseCase;
import com.connectedgames.identity.application.port.in.LogoutUseCase;
import com.connectedgames.identity.application.port.in.RefreshSessionUseCase;
import com.connectedgames.identity.application.port.in.RegisterUseCase;
import com.connectedgames.identity.application.port.out.AccessTokenIssuer;
import com.connectedgames.identity.application.port.out.GuestRepository;
import com.connectedgames.identity.application.port.out.PasswordHasher;
import com.connectedgames.identity.application.port.out.RefreshTokenGenerator;
import com.connectedgames.identity.application.port.out.RefreshTokenRepository;
import com.connectedgames.identity.application.port.out.SessionRepository;
import com.connectedgames.identity.application.port.out.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;

/**
 * Creates the use cases. The application layer has no Spring, so this is the only place where
 * Spring sees it, and where each use case gets its transaction: all its writes or none.
 */
@Configuration
class IdentityConfiguration {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    SessionTokens sessionTokens(RefreshTokenRepository refreshTokens, RefreshTokenGenerator tokenGenerator,
                                AccessTokenIssuer accessTokens) {
        return new SessionTokens(refreshTokens, tokenGenerator, accessTokens);
    }

    @Bean
    CreateGuestSessionUseCase createGuestSession(GuestRepository guests, SessionRepository sessions,
                                                 SessionTokens tokens, Clock clock, TransactionTemplate transaction) {
        CreateGuestSessionService service = new CreateGuestSessionService(guests, sessions, tokens, clock);
        return nickname -> transaction.execute(status -> service.createGuestSession(nickname));
    }

    @Bean
    LoginUseCase login(UserRepository users, PasswordHasher passwordHasher, SessionRepository sessions,
                       SessionTokens tokens, Clock clock, TransactionTemplate transaction) {
        LoginService service = new LoginService(users, passwordHasher, sessions, tokens, clock);
        return (email, password) -> transaction.execute(status -> service.login(email, password));
    }

    /**
     * No transaction around the refresh: when a used token comes back the session is revoked and
     * then the call fails with 401, and a rollback would undo that revocation. Two refreshes at the
     * same moment are stopped by the conditional update of the refresh token adapter.
     */
    @Bean
    RefreshSessionUseCase refreshSession(RefreshTokenRepository refreshTokens, RefreshTokenGenerator tokenGenerator,
                                         SessionRepository sessions, GuestRepository guests, UserRepository users,
                                         SessionTokens tokens, Clock clock) {
        return new RefreshSessionService(refreshTokens, tokenGenerator, sessions, guests, users, tokens, clock);
    }

    @Bean
    LogoutUseCase logout(SessionRepository sessions, Clock clock, TransactionTemplate transaction) {
        LogoutService service = new LogoutService(sessions, clock);
        return sessionId -> transaction.executeWithoutResult(status -> service.logout(sessionId));
    }

    @Bean
    RegisterUseCase register(UserRepository users, PasswordHasher passwordHasher, SessionRepository sessions,
                             SessionTokens tokens, Clock clock, TransactionTemplate transaction) {
        RegisterService service = new RegisterService(users, passwordHasher, sessions, tokens, clock);
        return (email, password, displayName, guestSession) ->
                transaction.execute(status -> service.register(email, password, displayName, guestSession));
    }

    @Bean
    GetCurrentUserUseCase getCurrentUser(UserRepository users) {
        return new GetCurrentUserService(users);
    }
}
