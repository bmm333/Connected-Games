package com.connectedgames.identity.application;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import com.connectedgames.identity.application.port.in.LoginUseCase;
import com.connectedgames.identity.application.port.in.UserSession;
import com.connectedgames.identity.application.port.out.PasswordHasher;
import com.connectedgames.identity.application.port.out.SessionRepository;
import com.connectedgames.identity.application.port.out.UserRepository;
import com.connectedgames.identity.domain.session.Session;
import com.connectedgames.identity.domain.session.SessionId;
import com.connectedgames.identity.domain.session.SessionKind;
import com.connectedgames.identity.domain.user.Email;
import com.connectedgames.identity.domain.user.User;

public final class LoginService implements LoginUseCase
{
    private final UserRepository users;
    private final PasswordHasher passwordHasher;
    private final SessionRepository sessions;
    private final SessionTokens tokens;
    private final Clock clock;

    public LoginService(UserRepository users,PasswordHasher passwordHasher,SessionRepository sessions,
            SessionTokens tokens,Clock clock)
    {
        this.users=users;
        this.passwordHasher=passwordHasher;
        this.sessions=sessions;
        this.tokens=tokens;
        this.clock=clock;
    }

    @Override
    public UserSession login(Email email,String password)
    {
        //it must not reveal who is registered
        User user=users.findByEmail(email).orElseThrow(InvalidCredentialsException::new);
        if(!passwordHasher.matches(password,user.passwordHash()))
        {
            throw new InvalidCredentialsException();
        }

        Instant now=clock.instant();

        Session session=Session.start(new SessionId(UUID.randomUUID()),SessionKind.USER,user.id().value(),now);
        sessions.save(session);

        return new UserSession(user,tokens.issue(session,user.roles(),now));
    }
}
